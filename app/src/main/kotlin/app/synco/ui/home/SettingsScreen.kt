package app.synco.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    state: HomeUiState,
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
        DeviceIdentityCard(
            identity = state.identity,
            displayName = state.displayName,
            onDisplayNameChange = actions::setDisplayName,
        )
        ReceivedFolderCard(
            receivedFolder = state.receivedFolder,
            onFolderChosen = actions::setReceivedFolder,
        )
        CaptureModeCard(
            mode = state.captureMode,
            shizukuState = state.shizukuState,
            adbHelper = state.adbShizukuHelper,
            startPending = state.shizukuStartPending,
            onModeChange = actions::setCaptureMode,
            onStartOverAdb = actions::requestShizukuStart,
        )
        CaptureTuningCard(
            tuning = state.captureTuning,
            mode = state.captureMode,
            shizukuPollMillis = state.shizukuPollMillis,
            onTuningChange = actions::setCaptureTuning,
            onShizukuPollChange = actions::setShizukuPollMillis,
        )
        BlobSizeCard(
            maxBlobBytes = state.maxBlobBytes,
            onMaxBlobBytesChange = actions::setMaxBlobBytes,
        )
        AboutCard()
    }
}
