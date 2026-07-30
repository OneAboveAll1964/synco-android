package app.synco.sync

import app.synco.clipboard.ClipboardCapture
import app.synco.clipboard.ClipboardCaptureStatus
import app.synco.clipboard.ForegroundClipListener
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal class ClipboardPipeline(
    private val capture: ClipboardCapture,
    private val foreground: ForegroundClipListener,
    private val dispatcher: OutboundClipDispatcher,
    private val state: SyncStateHolder,
) {
    suspend fun run(registry: PeerSessionRegistry) {
        coroutineScope {
            launch { foreground.run() }
            launch { capture.status.collect { publishStatus(it) } }
            capture.changes.collect { captured ->
                dispatcher.dispatch(captured.snapshot, registry.routers())
            }
        }
    }

    private fun publishStatus(status: ClipboardCaptureStatus) {
        if (status == ClipboardCaptureStatus.SERVICE_DISABLED) {
            state.raise(SyncProblem.CLIPBOARD_UNREADABLE)
        } else {
            state.clear(SyncProblem.CLIPBOARD_UNREADABLE)
        }
    }
}
