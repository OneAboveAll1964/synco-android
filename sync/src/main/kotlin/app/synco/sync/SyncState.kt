package app.synco.sync

import app.synco.protocol.DeviceId

data class SyncState(
    val running: Boolean = false,
    val paused: Boolean = false,
    val identity: DeviceIdentity? = null,
    val peers: List<PeerView> = emptyList(),
    val pendingPairings: List<PendingPairing> = emptyList(),
    val transfers: List<TransferView> = emptyList(),
    val lastEvent: SyncEvent? = null,
    val problem: SyncProblem? = null,
) {
    val connectedPeers: List<PeerView> get() = peers.filter { it.isConnected }

    val pairablePeers: List<PeerView> get() = peers.filter { it.isPairable }

    fun peer(deviceId: DeviceId): PeerView? = peers.firstOrNull { it.deviceId == deviceId }

    companion object {
        val IDLE = SyncState()
    }
}
