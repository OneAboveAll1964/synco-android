package app.synco.ui.history

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import app.synco.R
import app.synco.sync.SyncEvent

internal enum class HistoryTone { POSITIVE, NEUTRAL, NEGATIVE }

internal fun historyIcon(kind: SyncEvent.Kind): ImageVector = when (kind) {
    SyncEvent.Kind.CLIP_SENT -> Icons.Filled.ContentCopy
    SyncEvent.Kind.CLIP_ACKNOWLEDGED -> Icons.Filled.ContentCopy
    SyncEvent.Kind.CLIP_APPLIED -> Icons.Filled.ContentPaste
    SyncEvent.Kind.CLIP_DROPPED -> Icons.Filled.HighlightOff
    SyncEvent.Kind.CLIP_REFUSED -> Icons.Filled.Block
    SyncEvent.Kind.CLIP_DECLINED -> Icons.Filled.Block
    SyncEvent.Kind.TRANSFER_FAILED -> Icons.Filled.ErrorOutline
    SyncEvent.Kind.PEER_CONNECTED -> Icons.Filled.Link
    SyncEvent.Kind.PEER_DISCONNECTED -> Icons.Filled.LinkOff
    SyncEvent.Kind.PEER_PAIRED -> Icons.Filled.Verified
    SyncEvent.Kind.PEER_REJECTED -> Icons.Filled.Block
    SyncEvent.Kind.PAIRING_REQUESTED -> Icons.Filled.HelpOutline
    SyncEvent.Kind.ENGINE_FAILED -> Icons.Filled.ErrorOutline
}

internal fun historyTone(kind: SyncEvent.Kind): HistoryTone = when (kind) {
    SyncEvent.Kind.CLIP_SENT,
    SyncEvent.Kind.CLIP_ACKNOWLEDGED,
    SyncEvent.Kind.CLIP_APPLIED,
    SyncEvent.Kind.PEER_CONNECTED,
    SyncEvent.Kind.PEER_PAIRED,
    -> HistoryTone.POSITIVE

    SyncEvent.Kind.CLIP_DROPPED,
    SyncEvent.Kind.CLIP_REFUSED,
    SyncEvent.Kind.CLIP_DECLINED,
    SyncEvent.Kind.TRANSFER_FAILED,
    SyncEvent.Kind.PEER_REJECTED,
    SyncEvent.Kind.ENGINE_FAILED,
    -> HistoryTone.NEGATIVE

    SyncEvent.Kind.PEER_DISCONNECTED,
    SyncEvent.Kind.PAIRING_REQUESTED,
    -> HistoryTone.NEUTRAL
}

internal data class HistoryToneColors(val container: Color, val content: Color)

@Composable
internal fun historyToneColors(tone: HistoryTone): HistoryToneColors {
    val scheme = MaterialTheme.colorScheme
    return when (tone) {
        HistoryTone.POSITIVE -> HistoryToneColors(
            container = scheme.primaryContainer,
            content = scheme.onPrimaryContainer,
        )

        HistoryTone.NEUTRAL -> HistoryToneColors(
            container = scheme.surfaceContainerHighest,
            content = scheme.onSurfaceVariant,
        )

        HistoryTone.NEGATIVE -> HistoryToneColors(
            container = scheme.errorContainer,
            content = scheme.onErrorContainer,
        )
    }
}

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
