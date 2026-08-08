package app.synco.sync

import app.synco.clipboard.ClipboardCapture
import app.synco.clipboard.ClipboardCaptureStatus
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal class ClipboardPipeline(
    private val readsElsewhere: () -> Boolean,
    private val capture: ClipboardCapture,
    private val dispatcher: OutboundClipDispatcher,
    private val state: SyncStateHolder,
    private val history: ClipHistoryRecorder,
) {
    suspend fun run(registry: PeerSessionRegistry) {
        coroutineScope {
            launch { capture.status.collect { publishStatus(it) } }
            capture.changes.collect { captured ->
                history.local(captured.snapshot.reps)
                dispatcher.dispatch(captured.snapshot, registry.routers())
            }
        }
    }

    private fun publishStatus(status: ClipboardCaptureStatus) {
        if (readsElsewhere()) {
            state.clear(SyncProblem.CLIPBOARD_UNREADABLE)
            return
        }
        if (status == ClipboardCaptureStatus.SERVICE_DISABLED) {
            state.raise(SyncProblem.CLIPBOARD_UNREADABLE)
        } else {
            state.clear(SyncProblem.CLIPBOARD_UNREADABLE)
        }
    }
}
