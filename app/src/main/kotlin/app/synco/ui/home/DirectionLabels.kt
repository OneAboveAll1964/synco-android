package app.synco.ui.home

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.ui.graphics.vector.ImageVector
import app.synco.R
import app.synco.sync.SyncDirection

internal object DirectionLabels {

    val order: List<SyncDirection> = listOf(
        SyncDirection.BOTH,
        SyncDirection.OUTBOUND,
        SyncDirection.INBOUND,
        SyncDirection.NONE,
    )

    @StringRes
    fun labelOf(direction: SyncDirection): Int = when (direction) {
        SyncDirection.BOTH -> R.string.direction_both
        SyncDirection.OUTBOUND -> R.string.direction_outbound
        SyncDirection.INBOUND -> R.string.direction_inbound
        SyncDirection.NONE -> R.string.direction_none
    }

    @StringRes
    fun explanationOf(direction: SyncDirection): Int = when (direction) {
        SyncDirection.BOTH -> R.string.direction_both_explanation
        SyncDirection.OUTBOUND -> R.string.direction_outbound_explanation
        SyncDirection.INBOUND -> R.string.direction_inbound_explanation
        SyncDirection.NONE -> R.string.direction_none_explanation
    }

    fun iconOf(direction: SyncDirection): ImageVector = when (direction) {
        SyncDirection.BOTH -> Icons.Filled.SyncAlt
        SyncDirection.OUTBOUND -> Icons.Filled.ArrowUpward
        SyncDirection.INBOUND -> Icons.Filled.ArrowDownward
        SyncDirection.NONE -> Icons.Filled.PauseCircleOutline
    }
}
