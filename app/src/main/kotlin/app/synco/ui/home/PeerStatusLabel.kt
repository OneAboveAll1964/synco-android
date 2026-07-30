package app.synco.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.synco.R
import app.synco.sync.PeerConnectionStatus

@Composable
fun PeerStatusLabel(status: PeerConnectionStatus, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Surface(
            modifier = Modifier.size(8.dp),
            shape = CircleShape,
            color = indicatorColorOf(status),
            content = { },
        )
        Text(
            text = stringResource(labelOf(status)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 2.dp),
        )
    }
}

@Composable
private fun indicatorColorOf(status: PeerConnectionStatus): Color = when (status) {
    PeerConnectionStatus.CONNECTED -> MaterialTheme.colorScheme.primary
    PeerConnectionStatus.CONNECTING,
    PeerConnectionStatus.RETRYING,
    PeerConnectionStatus.PAIRING,
    PeerConnectionStatus.WAITING,
    -> MaterialTheme.colorScheme.tertiary

    PeerConnectionStatus.REJECTED -> MaterialTheme.colorScheme.error
    PeerConnectionStatus.DISCOVERED, PeerConnectionStatus.OFFLINE ->
        MaterialTheme.colorScheme.outlineVariant
}

private fun labelOf(status: PeerConnectionStatus): Int = when (status) {
    PeerConnectionStatus.OFFLINE -> R.string.peer_status_offline
    PeerConnectionStatus.DISCOVERED -> R.string.peer_status_discovered
    PeerConnectionStatus.WAITING -> R.string.peer_status_waiting
    PeerConnectionStatus.CONNECTING -> R.string.peer_status_connecting
    PeerConnectionStatus.RETRYING -> R.string.peer_status_retrying
    PeerConnectionStatus.PAIRING -> R.string.peer_status_pairing
    PeerConnectionStatus.CONNECTED -> R.string.peer_status_connected
    PeerConnectionStatus.REJECTED -> R.string.peer_status_rejected
}
