package app.synco.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.synco.R

@Composable
fun PeerList(peers: List<PeerRow>, actions: HomeActions, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = stringResource(R.string.peers_heading),
            style = MaterialTheme.typography.titleMedium,
        )
        if (peers.isEmpty()) {
            Text(
                text = stringResource(R.string.peers_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
        peers.forEach { peer ->
            PeerCard(
                peer = peer,
                onDirectionChange = { direction -> actions.setDirection(peer.deviceId, direction) },
                onSendChange = { category, enabled ->
                    actions.setSendEnabled(peer.deviceId, category, enabled)
                },
                onReceiveChange = { category, enabled ->
                    actions.setReceiveEnabled(peer.deviceId, category, enabled)
                },
                onReconnect = { actions.reconnectPeer(peer.deviceId) },
                onControl = { actions.controlRemote(peer.deviceId) },
                onForget = { actions.forgetPeer(peer.deviceId) },
            )
        }
    }
}
