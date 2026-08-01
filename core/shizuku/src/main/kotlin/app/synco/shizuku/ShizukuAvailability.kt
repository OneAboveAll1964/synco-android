package app.synco.shizuku

import android.content.Context
import android.content.pm.PackageManager
import app.synco.logging.SyncoLog
import rikka.shizuku.Shizuku

class ShizukuAvailability(context: Context) {

    private val packages = context.applicationContext.packageManager

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

    private fun isInstalled(): Boolean = PACKAGES.any { candidate ->
        runCatching {
            packages.getPackageInfo(candidate, 0)
            true
        }.getOrDefault(false)
    }

    private fun isRunning(): Boolean = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    private fun isGranted(): Boolean = runCatching {
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }.getOrDefault(false)

    private companion object {
        val PACKAGES = listOf("moe.shizuku.privileged.api", "moe.shizuku.manager")
    }
}
