package app.synco.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.synco.R
import app.synco.storage.ClipCategory
import app.synco.sync.SyncDirection

@Composable
fun PeerPolicyControls(
    peer: PeerRow,
    onDirectionChange: (SyncDirection) -> Unit,
    onSendChange: (ClipCategory, Boolean) -> Unit,
    onReceiveChange: (ClipCategory, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DirectionControl(direction = peer.direction, onDirectionChange = onDirectionChange)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            CategorySwitches(
                heading = stringResource(R.string.peer_send_heading),
                flags = peer.send,
                enabled = peer.direction.sends,
                onCategoryChange = onSendChange,
                modifier = Modifier.weight(1f),
            )
            CategorySwitches(
                heading = stringResource(R.string.peer_receive_heading),
                flags = peer.receive,
                enabled = peer.direction.receives,
                onCategoryChange = onReceiveChange,
                modifier = Modifier.weight(1f),
            )
        }
        PeerCapsNotice(peer)
    }
}
