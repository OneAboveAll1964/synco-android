package app.synco.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.synco.SyncServiceGateway
import app.synco.protocol.DeviceId
import app.synco.storage.ClipCategory
import app.synco.sync.SyncDirection
import app.synco.sync.SyncoGraph
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

class HomeViewModel(
    private val graph: SyncoGraph,
    private val service: SyncServiceGateway,
) : ViewModel(), HomeActions {

    val state: StateFlow<HomeUiState> = combine(
        graph.state,
        HomePreferences.flowOf(graph.settings),
        PeerPolicies.flowOf(graph.settings),
        graph.trustedPeers.peers,
        graph.clipboard.status,
    ) { sync, preferences, policies, trusted, clipboardStatus ->
        HomeStateMapper.map(sync, preferences, policies, trusted, clipboardStatus)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MILLIS),
        initialValue = HomeUiState.EMPTY,
    )

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

    override fun resetIdentity() {
        graph.commands.resetIdentity()
    }

    private companion object {
        const val SUBSCRIPTION_GRACE_MILLIS = 5_000L
    }
}
