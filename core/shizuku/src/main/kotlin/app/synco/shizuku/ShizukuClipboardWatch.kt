package app.synco.shizuku

import android.content.IOnPrimaryClipChangedListener
import app.synco.logging.SyncoLog

class ShizukuClipboardWatch {

    private var listener: IOnPrimaryClipChangedListener.Stub? = null

    private var service: Any? = null

    @Synchronized
    fun start(onChanged: () -> Unit): Boolean {
        if (listener != null) return true
        val connected = ClipboardService.connect() ?: return false
        val method = ClipboardService.methodNamed(connected, ADD) ?: return false
        val stub = object : IOnPrimaryClipChangedListener.Stub() {
            override fun dispatchPrimaryClipChanged() = onChanged()
        }
        val registered = runCatching {
            method.invoke(connected, *ClipboardService.argumentsFor(method, stub))
        }.onFailure { SyncoLog.clipboard.warn("Shizuku could not watch the clipboard", it) }.isSuccess
        if (!registered) return false
        listener = stub
        service = connected
        SyncoLog.clipboard.info("Shizuku is watching the clipboard, no polling needed")
        return true
    }

    @Synchronized
    fun stop() {
        val current = listener ?: return
        val connected = service
        listener = null
        service = null
        if (connected == null) return
        val method = ClipboardService.methodNamed(connected, REMOVE) ?: return
        runCatching { method.invoke(connected, *ClipboardService.argumentsFor(method, current)) }
    }

    @Synchronized
    fun isWatching(): Boolean = listener != null

    private companion object {
        const val ADD = "addPrimaryClipChangedListener"
        const val REMOVE = "removePrimaryClipChangedListener"
    }
}
