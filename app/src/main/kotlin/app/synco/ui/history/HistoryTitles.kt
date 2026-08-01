package app.synco.ui.history

import androidx.annotation.StringRes
import app.synco.R
import app.synco.sync.SyncEvent

@StringRes
internal fun historyTitleRes(kind: SyncEvent.Kind): Int = when (kind) {
    SyncEvent.Kind.CLIP_SENT -> R.string.history_clip_sent
    SyncEvent.Kind.CLIP_DROPPED -> R.string.history_clip_dropped
    SyncEvent.Kind.CLIP_ACKNOWLEDGED -> R.string.history_clip_acknowledged
    SyncEvent.Kind.CLIP_REFUSED -> R.string.history_clip_refused
    SyncEvent.Kind.CLIP_APPLIED -> R.string.history_clip_applied
    SyncEvent.Kind.CLIP_DECLINED -> R.string.history_clip_declined
    SyncEvent.Kind.TRANSFER_FAILED -> R.string.history_transfer_failed
    SyncEvent.Kind.PEER_CONNECTED -> R.string.history_peer_connected
    SyncEvent.Kind.PEER_DISCONNECTED -> R.string.history_peer_disconnected
    SyncEvent.Kind.PEER_PAIRED -> R.string.history_peer_paired
    SyncEvent.Kind.PEER_REJECTED -> R.string.history_peer_rejected
    SyncEvent.Kind.PAIRING_REQUESTED -> R.string.history_pairing_requested
    SyncEvent.Kind.ENGINE_FAILED -> R.string.history_engine_failed
}
