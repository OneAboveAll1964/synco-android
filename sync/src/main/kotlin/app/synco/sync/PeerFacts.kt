package app.synco.sync

import app.synco.crypto.HandshakeRole
import app.synco.discovery.DiscoveredPeer
import app.synco.protocol.DeviceId
import app.synco.protocol.message.Caps
import app.synco.protocol.message.CloseReason
import app.synco.storage.SyncPolicy
import app.synco.storage.TrustedPeer
import app.synco.transport.PeerDescriptor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

internal class PeerFacts(deviceId: DeviceId, private val role: HandshakeRole, policy: SyncPolicy) {

    private val current = MutableStateFlow(
        PeerView(
            deviceId = deviceId,
            displayName = deviceId.value,
            platform = null,
            fingerprint = null,
            status = PeerConnectionStatus.OFFLINE,
            policy = policy,
            trusted = false,
            rejected = false,
            discovered = false,
            dials = role.dials,
            peerCaps = null,
            lastCloseReason = null,
        ),
    )

    private val discovered = MutableStateFlow<DiscoveredPeer?>(null)

    private val rejected = MutableStateFlow(false)

    @Volatile
    private var pairing = false

    val view: StateFlow<PeerView> = current.asStateFlow()

    fun setDiscovered(peer: DiscoveredPeer?) {
        discovered.value = peer
        current.update {
            it.copy(
                discovered = peer != null,
                displayName = peer?.displayName ?: it.displayName,
                platform = peer?.platform ?: it.platform,
                fingerprint = peer?.fingerprint ?: it.fingerprint,
            )
        }
    }

    fun setTrusted(record: TrustedPeer?) {
        rejected.value = record?.rejected == true
        current.update {
            it.copy(
                trusted = record?.isTrusted == true,
                rejected = record?.rejected == true,
                displayName = record?.displayName ?: it.displayName,
                platform = record?.platform ?: it.platform,
                fingerprint = record?.fingerprint ?: it.fingerprint,
            )
        }
    }

    fun setPairing(pending: Boolean) {
        pairing = pending
    }

    fun setPolicy(policy: SyncPolicy) {
        current.update { it.copy(policy = policy) }
    }

    fun setCaps(caps: Caps?) {
        current.update { it.copy(peerCaps = caps) }
    }

    fun setStatus(status: PeerConnectionStatus) {
        current.update { it.copy(status = status) }
    }

    fun connected(peer: PeerDescriptor) {
        current.update {
            it.copy(
                status = PeerConnectionStatus.CONNECTED,
                displayName = peer.displayName,
                platform = peer.platform,
                fingerprint = peer.fingerprint,
                lastCloseReason = null,
            )
        }
    }

    fun disconnected(status: PeerConnectionStatus, reason: CloseReason?) {
        current.update {
            it.copy(status = status, peerCaps = null, lastCloseReason = reason ?: it.lastCloseReason)
        }
    }

    fun idleStatus(): PeerConnectionStatus = when {
        pairing -> PeerConnectionStatus.PAIRING
        rejected.value -> PeerConnectionStatus.REJECTED
        discovered.value == null -> PeerConnectionStatus.OFFLINE
        role.dials -> PeerConnectionStatus.DISCOVERED
        else -> PeerConnectionStatus.WAITING
    }

    fun dialTargets(live: Flow<Boolean>): Flow<DiscoveredPeer> =
        combine(discovered, rejected, live) { peer, isRejected, isLive ->
            peer.takeIf { DialRule.intentOf(role, peer != null, isRejected, isLive) == PeerIntent.DIAL }
        }.filterNotNull()

    fun disappearances(): Flow<Unit> = discovered.filter { it == null }.map { }
}
