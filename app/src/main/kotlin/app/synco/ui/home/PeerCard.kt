package app.synco.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.synco.R
import app.synco.storage.ClipCategory
import app.synco.sync.SyncDirection

@Composable
fun PeerCard(
    peer: PeerRow,
    onDirectionChange: (SyncDirection) -> Unit,
    onSendChange: (ClipCategory, Boolean) -> Unit,
    onReceiveChange: (ClipCategory, Boolean) -> Unit,
    onReconnect: () -> Unit,
    onForget: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PeerHeader(peer)
            HorizontalDivider()
            if (peer.trusted) {
                PeerPolicyControls(
                    peer = peer,
                    onDirectionChange = onDirectionChange,
                    onSendChange = onSendChange,
                    onReceiveChange = onReceiveChange,
                )
            } else {
                Text(
                    text = stringResource(R.string.peer_unpaired_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onReconnect, enabled = peer.trusted && !peer.isConnected) {
                    Text(text = stringResource(R.string.peer_reconnect))
                }
                TextButton(onClick = onForget, enabled = peer.trusted || peer.isRejected) {
                    Text(text = stringResource(R.string.peer_forget))
                }
            }
        }
    }
}

@Composable
private fun PeerHeader(peer: PeerRow) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = peer.displayName, style = MaterialTheme.typography.titleMedium)
            Text(
                text = peer.fingerprint?.grouped ?: peer.deviceId.value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        PeerStatusLabel(status = peer.status)
    }
}
