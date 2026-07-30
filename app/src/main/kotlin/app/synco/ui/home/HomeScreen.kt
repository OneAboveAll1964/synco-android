package app.synco.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.synco.ui.permissions.PermissionsCard
import app.synco.ui.permissions.PermissionsController

@Composable
fun HomeScreen(
    state: HomeUiState,
    statusText: String,
    permissions: PermissionsController,
    actions: HomeActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SyncControlCard(
            running = state.running,
            paused = state.paused,
            launchOnBoot = state.launchOnBoot,
            statusText = statusText,
            onRunningChange = actions::setRunning,
            onPausedChange = actions::setPaused,
            onLaunchOnBootChange = actions::setLaunchOnBoot,
        )
        Text(
            text = stringResource(clipboardStatusTextRes(state.clipboardStatus)),
            style = MaterialTheme.typography.bodySmall,
        )
        if (permissions.hasBlockers || state.problem != null) {
            PermissionsCard(
                missing = permissions.missing,
                problem = state.problem,
                onRequest = permissions::request,
                onResetIdentity = actions::resetIdentity,
            )
        }
        DeviceIdentityCard(
            identity = state.identity,
            displayName = state.displayName,
            onDisplayNameChange = actions::setDisplayName,
        )
        ReceivedFolderCard(
            receivedFolder = state.receivedFolder,
            onFolderChosen = actions::setReceivedFolder,
        )
        if (state.transfers.isNotEmpty()) {
            TransferList(transfers = state.transfers, onCancel = actions::cancelTransfer)
        }
        PeerList(peers = state.peers, actions = actions)
    }
}
