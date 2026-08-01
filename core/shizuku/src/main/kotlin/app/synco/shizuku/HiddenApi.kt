package app.synco.shizuku

import android.os.Build
import app.synco.logging.SyncoLog
import org.lsposed.hiddenapibypass.HiddenApiBypass

object HiddenApi {

    @Volatile
    private var opened = false

    @Synchronized
    fun open() {
        if (opened) return
        opened = true
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        val granted = runCatching { HiddenApiBypass.addHiddenApiExemptions("") }
            .onFailure { SyncoLog.clipboard.warn("could not lift the hidden API blocklist", it) }
            .getOrDefault(false)
        SyncoLog.clipboard.info("hidden API access ${if (granted) "granted" else "refused"}")
    }
}
