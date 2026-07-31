package app.synco.shizuku

import android.content.Context
import android.content.pm.PackageManager
import app.synco.logging.SyncoLog
import rikka.shizuku.Shizuku

class ShizukuAvailability(context: Context) {

    private val packages = context.applicationContext.packageManager

    fun state(): ShizukuState {
        if (!isInstalled()) return ShizukuState.NOT_INSTALLED
        if (!isRunning()) return ShizukuState.NOT_RUNNING
        return if (isGranted()) ShizukuState.READY else ShizukuState.PERMISSION_DENIED
    }

    fun requestPermission(requestCode: Int) {
        runCatching { Shizuku.requestPermission(requestCode) }
            .onFailure { SyncoLog.clipboard.warn("could not ask Shizuku for permission", it) }
    }

    private fun isInstalled(): Boolean = runCatching {
        packages.getPackageInfo(SHIZUKU_PACKAGE, 0)
        true
    }.getOrDefault(false)

    private fun isRunning(): Boolean = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    private fun isGranted(): Boolean = runCatching {
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }.getOrDefault(false)

    private companion object {
        const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
    }
}
