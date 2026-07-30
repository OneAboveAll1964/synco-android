package app.synco.sync

import app.synco.protocol.DeviceId
import app.synco.protocol.ProtocolConstants
import app.synco.transport.PeerDescriptor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap

internal class PairingRequests(
    private val timeoutMillis: Long = ProtocolConstants.PAIR_TIMEOUT_MILLIS,
) {
    private val descriptors = ConcurrentHashMap<DeviceId, PeerDescriptor>()
    private val decisions = ConcurrentHashMap<DeviceId, CompletableDeferred<Boolean>>()
    private val waiting = MutableStateFlow<List<PendingPairing>>(emptyList())

    val pending: StateFlow<List<PendingPairing>> = waiting.asStateFlow()

    fun remember(peer: PeerDescriptor) {
        descriptors[peer.deviceId] = peer
    }

    fun descriptorOf(deviceId: DeviceId): PeerDescriptor? = descriptors[deviceId]

    suspend fun await(peer: PeerDescriptor, requestedAtMillis: Long): Boolean {
        val decision = CompletableDeferred<Boolean>()
        decisions[peer.deviceId] = decision
        waiting.update { current ->
            current.filterNot { it.deviceId == peer.deviceId } + PendingPairing.of(peer, requestedAtMillis)
        }
        val approved = withTimeoutOrNull(timeoutMillis) { decision.await() } ?: false
        decisions.remove(peer.deviceId)
        withdraw(peer.deviceId)
        return approved
    }

    fun settle(deviceId: DeviceId, approved: Boolean) {
        decisions[deviceId]?.complete(approved)
    }

    fun forget(deviceId: DeviceId) {
        descriptors.remove(deviceId)
        decisions.remove(deviceId)
        withdraw(deviceId)
    }

    private fun withdraw(deviceId: DeviceId) {
        waiting.update { current -> current.filterNot { it.deviceId == deviceId } }
    }
}
