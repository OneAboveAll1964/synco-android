package app.synco.shizuku

import app.synco.logging.SyncoLog
import rikka.shizuku.Shizuku

object ShizukuPermission {

    const val REQUEST_CODE = 4371

    private var listener: Shizuku.OnRequestPermissionResultListener? = null

    fun observe(onResult: (Boolean) -> Unit) {
        if (listener != null) return
        val created = Shizuku.OnRequestPermissionResultListener { code, grantResult ->
            if (code != REQUEST_CODE) return@OnRequestPermissionResultListener
            val granted = grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED
            SyncoLog.clipboard.info("Shizuku permission ${if (granted) "granted" else "refused"}")
            onResult(granted)
        }
        runCatching { Shizuku.addRequestPermissionResultListener(created) }
            .onSuccess { listener = created }
            .onFailure { SyncoLog.clipboard.warn("could not observe Shizuku permission results", it) }
    }

    fun stop() {
        val current = listener ?: return
        runCatching { Shizuku.removeRequestPermissionResultListener(current) }
        listener = null
    }
}
