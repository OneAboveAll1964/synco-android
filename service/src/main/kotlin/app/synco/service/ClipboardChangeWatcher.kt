package app.synco.service

import android.content.ClipboardManager
import android.content.Context
import app.synco.logging.SyncoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class ClipboardChangeWatcher(context: Context, private val scope: CoroutineScope) {

    private val clipboard = context.getSystemService(ClipboardManager::class.java)

    private val fired = AtomicInteger()

    private val changes = MutableSharedFlow<Unit>(
        extraBufferCapacity = BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val listener = ClipboardManager.OnPrimaryClipChangedListener {
        SyncoLog.clipboard.info("clipboard change signalled, total=${fired.incrementAndGet()}")
        changes.tryEmit(Unit)
    }

    fun start() {
        val manager = clipboard
        if (manager == null) {
            SyncoLog.clipboard.warn("this device has no ClipboardManager, copies cannot be observed")
            return
        }
        runCatching { manager.addPrimaryClipChangedListener(listener) }
            .onSuccess { SyncoLog.clipboard.info("watching the clipboard for changes") }
            .onFailure { SyncoLog.clipboard.warn("could not observe clipboard changes", it) }
        scope.launch {
            changes.collectLatest {
                SyncoLog.clipboard.info("capturing after a clipboard change")
                FocusGateHolder.captureNow()
            }
        }
    }

    fun stop() {
        runCatching { clipboard?.removePrimaryClipChangedListener(listener) }
    }

    private companion object {
        const val BUFFER = 8
    }
}
