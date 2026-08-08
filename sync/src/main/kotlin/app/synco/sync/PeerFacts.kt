package app.synco.sync

import app.synco.crypto.HandshakeRole
import app.synco.discovery.DiscoveredPeer
import app.synco.protocol.DeviceId
import app.synco.protocol.message.Caps
import app.synco.protocol.message.CloseReason
import app.synco.storage.SyncPolicy
import app.synco.storage.TrustedPeer
import app.synco.transport.PeerDescriptor
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
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

    private val trusted = MutableStateFlow(false)

    private val manual = MutableStateFlow<TrustedPeer?>(null)

    private val manualCursor = AtomicInteger(0)

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
        trusted.value = record?.isTrusted == true
        manual.value = record?.takeIf { it.isTrusted && it.manualHostList.isNotEmpty() }
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
        discovered.value == null && manual.value == null -> PeerConnectionStatus.OFFLINE
        !trusted.value && manual.value == null -> PeerConnectionStatus.WAITING
        role.dials || manual.value != null -> PeerConnectionStatus.DISCOVERED
        else -> PeerConnectionStatus.WAITING
    }

    fun dialTargets(live: Flow<Boolean>): Flow<DiscoveredPeer> =
        combine(discovered, rejected, live, trusted, manual) { peer, isRejected, isLive, isTrusted, fallback ->
            val intent = DialRule.intentOf(
                role = role,
                discovered = peer != null,
                rejected = isRejected,
                live = isLive,
                trusted = isTrusted,
                adoptedManually = fallback != null,
            )
            when {
                peer != null && intent == PeerIntent.DIAL -> peer
                peer == null && !isLive && !isRejected && fallback != null -> synthesize(fallback)
                else -> null
            }
        }.filterNotNull()

    private fun synthesize(record: TrustedPeer): DiscoveredPeer? {
        val fingerprint = record.fingerprint ?: return null
        val hosts = record.manualHostList
        val host = hosts[manualCursor.getAndIncrement() % hosts.size]
        return DiscoveredPeer(
            deviceId = record.deviceId,
            displayName = record.displayName,
            platform = record.platform,
            fingerprint = fingerprint,
            host = host,
            port = record.manualPort ?: return null,
            lastSeen = Instant.now(),
        )
    }

    fun disappearances(): Flow<Unit> = discovered.filter { it == null }.map { }
}
