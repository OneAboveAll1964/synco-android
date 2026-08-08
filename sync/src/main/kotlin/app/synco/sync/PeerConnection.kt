package app.synco.sync

import app.synco.crypto.HandshakeRole
import app.synco.discovery.DiscoveredPeer
import app.synco.protocol.DeviceId
import app.synco.protocol.message.Caps
import app.synco.protocol.message.CloseReason
import app.synco.protocol.message.PolicySync
import app.synco.storage.SyncPolicy
import app.synco.storage.TrustedPeer
import app.synco.transport.Backoff
import app.synco.transport.PeerDescriptor
import app.synco.transport.PeerSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class PeerConnection(
    val peerDeviceId: DeviceId,
    role: HandshakeRole,
    private val settings: PeerSettings,
    private val slot: PeerSessionSlot,
    dialer: PeerDialer,
    runner: PeerSessionRunner,
    private val events: SyncEventSink,
    private val scope: CoroutineScope,
    private val backoff: Backoff = Backoff(),
    private val policies: PolicyExchange? = null,
    private val media: RemoteMediaSink = RemoteMediaSink.NONE,
) {
    private val facts = PeerFacts(peerDeviceId, role, settings.policy)

    private val dialLoop = PeerDialLoop(this, facts, slot, dialer, runner, backoff)

    private var loop: Job? = null

    val view: StateFlow<PeerView> = facts.view

    val router: ClipRouter? get() = slot.router

    fun start() {
        if (loop == null) loop = scope.launch { dialLoop.run() }
    }

    fun onDiscovered(peer: DiscoveredPeer?) {
        facts.setDiscovered(peer)
        refreshStatus()
    }

    fun onTrusted(record: TrustedPeer?) {
        facts.setTrusted(record)
        refreshStatus()
    }

    fun onPairing(pending: Boolean) {
        facts.setPairing(pending)
        refreshStatus()
    }

    suspend fun onPeerPolicy(policy: PolicySync) {
        policies?.adopt(peerDeviceId, policy)
    }

    suspend fun publishPolicy() {
        val exchange = policies ?: return
        slot.sendPolicy(exchange.localPolicy(peerDeviceId), settings.policy)
    }

    fun onPeerCaps(caps: Caps) {
        settings.peerCaps = caps
        facts.setCaps(caps)
    }

    suspend fun onPolicy(policy: SyncPolicy) {
        if (settings.policy == policy) return
        settings.policy = policy
        facts.setPolicy(policy)
        slot.sendCaps()
        publishPolicy()
    }

    suspend fun claim(
        peer: PeerDescriptor,
        origin: SessionOrigin,
        session: PeerSession,
    ): SessionBinding? {
        val claimed = slot.claim(session, origin) ?: return null
        backoff.reset()
        facts.connected(peer)
        slot.sendCaps()
        publishPolicy()
        events.record(SyncEvent.of(SyncEvent.Kind.PEER_CONNECTED, peerDeviceId))
        return SessionBinding(this, session, claimed.router, media)
    }

    suspend fun release(session: PeerSession, reason: CloseReason?) {
        if (!slot.release(session)) return
        facts.disconnected(facts.idleStatus(), reason)
        events.record(SyncEvent.of(SyncEvent.Kind.PEER_DISCONNECTED, peerDeviceId, reason?.wireValue))
    }

    fun reconnect() {
        backoff.reset()
        dialLoop.nudge()
    }

    suspend fun close(reason: CloseReason) {
        loop?.cancelAndJoin()
        loop = null
        slot.close(reason)
        facts.disconnected(PeerConnectionStatus.OFFLINE, reason)
    }

    private fun refreshStatus() {
        if (dialLoop.dialing || slot.live.value) return
        facts.setStatus(facts.idleStatus())
    }
}
