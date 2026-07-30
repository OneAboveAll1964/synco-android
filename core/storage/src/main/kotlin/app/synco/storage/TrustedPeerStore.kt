package app.synco.storage

import app.synco.protocol.DeviceId
import kotlinx.coroutines.flow.Flow

interface TrustedPeerStore {

    val peers: Flow<List<TrustedPeer>>

    suspend fun add(peer: TrustedPeer)

    suspend fun remove(deviceId: DeviceId)

    suspend fun find(deviceId: DeviceId): TrustedPeer?

    suspend fun updateDisplayName(deviceId: DeviceId, displayName: String)

    suspend fun setRejected(deviceId: DeviceId, rejected: Boolean)
}
