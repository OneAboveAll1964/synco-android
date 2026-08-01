package app.synco.ui.home

import app.synco.clipboard.ClipboardCaptureStatus
import app.synco.protocol.ProtocolConstants
import app.synco.shizuku.ShizukuState
import app.synco.storage.CaptureMode
import app.synco.storage.CaptureTuning
import app.synco.storage.ShizukuPollChoice
import app.synco.sync.DeviceIdentity
import app.synco.sync.PendingPairing
import app.synco.sync.SyncProblem
import app.synco.sync.TransferView

data class HomeUiState(
    val running: Boolean,
    val paused: Boolean,
    val launchOnBoot: Boolean,
    val displayName: String,
    val receivedFolder: String?,
    val maxBlobBytes: Long,
    val captureTuning: CaptureTuning,
    val captureMode: CaptureMode,
    val shizukuPollMillis: Long,
    val shizukuState: ShizukuState,
    val identity: DeviceIdentity?,
    val peers: List<PeerRow>,
    val transfers: List<TransferView>,
    val pendingPairing: PendingPairing?,
    val problem: SyncProblem?,
    val clipboardStatus: ClipboardCaptureStatus,
) {
    val connectedPeers: List<PeerRow> get() = peers.filter { it.isConnected }

    companion object {
        val EMPTY = HomeUiState(
            running = false,
            paused = false,
            launchOnBoot = false,
            displayName = "",
            receivedFolder = null,
            maxBlobBytes = ProtocolConstants.DEFAULT_MAX_BLOB_BYTES,
            captureTuning = CaptureTuning.DEFAULT,
            captureMode = CaptureMode.DEFAULT,
            shizukuPollMillis = ShizukuPollChoice.DEFAULT.millis,
            shizukuState = ShizukuState.NOT_INSTALLED,
            identity = null,
            peers = emptyList(),
            transfers = emptyList(),
            pendingPairing = null,
            problem = null,
            clipboardStatus = ClipboardCaptureStatus.SERVICE_DISABLED,
        )
    }
}
