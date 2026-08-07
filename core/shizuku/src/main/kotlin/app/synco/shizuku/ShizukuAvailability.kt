package app.synco.shizuku

import android.content.Context
import android.content.pm.PackageManager
import app.synco.logging.SyncoLog
import rikka.shizuku.Shizuku

class ShizukuAvailability(context: Context) {

    private val packages = context.applicationContext.packageManager

    private val install = ShizukuInstall(
        hasPackage = { name ->
            runCatching { packages.getPackageInfo(name, 0) }.isSuccess ||
                runCatching { packages.getApplicationInfo(name, 0) }.isSuccess
        },
        hasProvider = { authority ->
            runCatching { packages.resolveContentProvider(authority, 0) }.getOrNull() != null
        },
    )

    @Volatile
    private var everSawBinder = false

    fun isInstalled(): Boolean = install.isInstalled(everSawBinder || isRunning())

    fun stateWithoutReading(): ShizukuState {
        if (!isRunning()) {
            return if (isInstalled()) ShizukuState.NOT_RUNNING else ShizukuState.NOT_INSTALLED
        }
        return if (isGranted()) ShizukuState.READY else ShizukuState.PERMISSION_DENIED
    }

    fun state(probe: () -> ShizukuRead): ShizukuState {
        if (!isRunning()) {
            return if (isInstalled()) ShizukuState.NOT_RUNNING else ShizukuState.NOT_INSTALLED
        }
        if (!isGranted()) return ShizukuState.PERMISSION_DENIED
        return when (probe()) {
            is ShizukuRead.Clip -> ShizukuState.READY
            ShizukuRead.Denied -> ShizukuState.PERMISSION_DENIED
            ShizukuRead.Unavailable -> ShizukuState.NOT_RUNNING
        }
    }

    fun requestPermission(requestCode: Int) {
        runCatching { Shizuku.requestPermission(requestCode) }
            .onFailure { SyncoLog.clipboard.warn("could not ask Shizuku for permission", it) }
    }

    fun shouldExplain(): Boolean =
        runCatching { Shizuku.shouldShowRequestPermissionRationale() }.getOrDefault(false)

    private fun isRunning(): Boolean = runCatching { Shizuku.pingBinder() }
        .getOrDefault(false)
        .also { if (it) everSawBinder = true }

    private fun isGranted(): Boolean = runCatching {
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }.getOrDefault(false)

}
