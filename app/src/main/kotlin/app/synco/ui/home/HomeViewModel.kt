package app.synco.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.synco.SyncServiceGateway
import app.synco.service.ShizukuStatus
import app.synco.protocol.DeviceId
import app.synco.storage.CaptureMode
import app.synco.storage.CaptureTuning
import app.synco.storage.ClipCategory
import app.synco.sync.ShizukuStartReport
import app.synco.sync.QRPairPayload
import app.synco.sync.SyncDirection
import app.synco.sync.SyncoGraph
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class HomeViewModel(
    private val graph: SyncoGraph,
    private val service: SyncServiceGateway,
) : ViewModel(), HomeActions {

    private val pending = MutableStateFlow(false)

    val state: StateFlow<HomeUiState> = combine(
        graph.state,
        HomePreferences.flowOf(graph.settings),
        PeerPolicies.flowOf(graph.settings),
        graph.trustedPeers.peers,
        combine(graph.clipboard.status, ShizukuStatus.state, pending, ::Triple),
    ) { sync, preferences, policies, trusted, capture ->
        HomeStateMapper.map(
            sync,
            preferences,
            policies,
            trusted,
            capture.first,
            capture.second,
            capture.third,
        )
    }.combine(graph.clipHistory.entries) { ui, clips -> ui.copy(clips = clips) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MILLIS),
        initialValue = HomeUiState.EMPTY,
    )

    init {
        viewModelScope.launch {
            graph.state
                .map { it.shizukuStart }
                .filterNotNull()
                .distinctUntilChanged()
                .collect(::onShizukuStart)
        }
    }

    private fun onShizukuStart(report: ShizukuStartReport) {
        pending.value = false
        if (report.started) graph.commands.setCaptureMode(CaptureMode.SHIZUKU)
    }

    override fun setRunning(running: Boolean) {
        if (running) service.start() else service.stop()
    }

    override fun setPaused(paused: Boolean) {
        graph.commands.setPaused(paused)
    }

    override fun setLaunchOnBoot(enabled: Boolean) {
        graph.commands.setLaunchOnBoot(enabled)
    }

    override fun setDisplayName(displayName: String) {
        graph.commands.setDisplayName(displayName)
    }

    override fun setReceivedFolder(treeUri: String?) {
        graph.commands.setReceivedFolder(treeUri)
    }

    override fun setMaxBlobBytes(bytes: Long) {
        graph.commands.setMaxBlobBytes(bytes)
    }

    override fun setCaptureTuning(tuning: CaptureTuning) {
        graph.commands.setCaptureTuning(tuning)
    }

    override fun applyHistoryClip(entry: app.synco.storage.ClipHistoryEntry) {
        graph.commands.applyHistoryClip(entry.text)
    }

    override fun pairWithQR(payload: QRPairPayload) {
        graph.commands.adoptQRPeer(payload)
    }

    val remoteState get() = graph.remote.state

    override fun controlRemote(deviceId: DeviceId) {
        graph.remote.connect(deviceId, REMOTE_MAX_WIDTH, REMOTE_MAX_HEIGHT, REMOTE_FPS)
    }

    fun remoteSurfaceReady(surface: android.view.Surface) = graph.remote.attachSurface(surface)

    fun remoteSurfaceLost() = graph.remote.detachSurface()

    fun remoteInput(events: List<app.synco.protocol.message.RemoteInputEvent>) = graph.remote.sendInput(events)

    fun disconnectRemote() = graph.remote.disconnect()

    override fun setCaptureMode(mode: CaptureMode) {
        graph.commands.setCaptureMode(mode)
    }


    override fun requestShizukuStart(deviceId: DeviceId) {
        pending.value = true
        graph.commands.requestShizukuStart(deviceId)
    }

    override fun clearShizukuStart() {
        graph.commands.clearShizukuStart()
    }

    override fun setDirection(deviceId: DeviceId, direction: SyncDirection) {
        graph.commands.setDirection(deviceId, direction)
    }

    override fun setSendEnabled(deviceId: DeviceId, category: ClipCategory, enabled: Boolean) {
        graph.commands.setSendEnabled(deviceId, category, enabled)
    }

    override fun setReceiveEnabled(deviceId: DeviceId, category: ClipCategory, enabled: Boolean) {
        graph.commands.setReceiveEnabled(deviceId, category, enabled)
    }

    override fun approvePairing(deviceId: DeviceId) {
        graph.commands.approvePairing(deviceId)
    }

    override fun rejectPairing(deviceId: DeviceId) {
        graph.commands.rejectPairing(deviceId)
    }

    override fun forgetPeer(deviceId: DeviceId) {
        graph.commands.forgetPeer(deviceId)
    }

    override fun reconnectPeer(deviceId: DeviceId) {
        graph.commands.reconnectPeer(deviceId)
    }

    override fun cancelTransfer(transferId: UUID) {
        graph.commands.cancelTransfer(transferId)
    }

    override fun sendText(value: String) {
        graph.commands.sendText(value)
    }

    override fun sendFile(uri: android.net.Uri) {
        graph.commands.sendFile(uri)
    }

    override fun resetIdentity() {
        graph.commands.resetIdentity()
    }

    private companion object {
        const val REMOTE_MAX_WIDTH = 1280
        const val REMOTE_MAX_HEIGHT = 2400
        const val REMOTE_FPS = 60
        const val SUBSCRIPTION_GRACE_MILLIS = 5_000L
    }
}
