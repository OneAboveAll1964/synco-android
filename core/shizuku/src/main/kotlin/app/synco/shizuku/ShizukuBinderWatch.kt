package app.synco.shizuku

import app.synco.logging.SyncoLog
import rikka.shizuku.Shizuku

object ShizukuBinderWatch {

    private var received: Shizuku.OnBinderReceivedListener? = null

    private var dead: Shizuku.OnBinderDeadListener? = null

    fun observe(onChanged: () -> Unit) {
        if (received != null) return
        val arrived = Shizuku.OnBinderReceivedListener {
            SyncoLog.clipboard.info("Shizuku is available")
            onChanged()
        }
        val lost = Shizuku.OnBinderDeadListener {
            SyncoLog.clipboard.info("Shizuku went away")
            onChanged()
        }
        runCatching {
            Shizuku.addBinderReceivedListenerSticky(arrived)
            Shizuku.addBinderDeadListener(lost)
        }.onSuccess {
            received = arrived
            dead = lost
        }.onFailure { SyncoLog.clipboard.warn("could not watch Shizuku", it) }
    }

    fun stop() {
        received?.let { runCatching { Shizuku.removeBinderReceivedListener(it) } }
        dead?.let { runCatching { Shizuku.removeBinderDeadListener(it) } }
        received = null
        dead = null
    }
}
