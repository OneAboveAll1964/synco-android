package app.synco.sync

import app.synco.protocol.DeviceId
import app.synco.storage.TrustedPeer
import app.synco.storage.TrustedPeerStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class RecordingTrustedPeers : TrustedPeerStore {

    private val stored = MutableStateFlow<Map<DeviceId, TrustedPeer>>(emptyMap())

    override val peers: Flow<List<TrustedPeer>> = stored.map { it.values.toList() }

    override suspend fun add(peer: TrustedPeer) {
        stored.value = stored.value + (peer.deviceId to peer)
    }

    override suspend fun remove(deviceId: DeviceId) {
        stored.value = stored.value - deviceId
    }

    override suspend fun find(deviceId: DeviceId): TrustedPeer? = stored.value[deviceId]

    override suspend fun updateDisplayName(deviceId: DeviceId, displayName: String) {
        mutate(deviceId) { it.copy(displayName = displayName) }
    }

    override suspend fun setRejected(deviceId: DeviceId, rejected: Boolean) {
        mutate(deviceId) { it.copy(rejected = rejected) }
    }

    private fun mutate(deviceId: DeviceId, transform: (TrustedPeer) -> TrustedPeer) {
        val existing = stored.value[deviceId] ?: return
        stored.value = stored.value + (deviceId to transform(existing))
    }
}
