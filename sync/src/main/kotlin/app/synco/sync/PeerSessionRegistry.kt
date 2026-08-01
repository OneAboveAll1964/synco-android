package app.synco.sync

import app.synco.discovery.DiscoveredPeer
import app.synco.protocol.DeviceId
import app.synco.protocol.message.CloseReason
import app.synco.storage.SyncPolicy
import app.synco.storage.TrustedPeer
import app.synco.transport.PairingResult
import app.synco.transport.PeerDescriptor
import app.synco.transport.PeerSession
import app.synco.transport.SessionOutcome
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

internal class PeerSessionRegistry(
    private val selfDeviceId: DeviceId,
    routers: ClipRouterFactory,
    dialer: PeerDialer,
    events: SyncEventSink,
    scope: CoroutineScope,
    private val pairings: PairingCoordinator,
    private val exchange: PolicyExchange? = null,
) : SessionHosts {

    private val policies = PolicyBook()
    private val runner = PeerSessionRunner(this)
    private val directory = PeerDirectory(
        PeerConnectionFactory(selfDeviceId, policies, routers, dialer, runner, events, scope, exchange),
    )

    val views: Flow<List<PeerView>> get() = directory.views

    fun routers(): List<ClipRouter> = directory.all.mapNotNull { it.router }

    fun applyDiscovered(peers: List<DiscoveredPeer>) {
        val byDeviceId = peers.filterNot { it.deviceId == selfDeviceId }.associateBy { it.deviceId }
        byDeviceId.forEach { (deviceId, peer) -> directory.of(deviceId).onDiscovered(peer) }
        directory.all
            .filterNot { it.peerDeviceId in byDeviceId }
            .forEach { it.onDiscovered(null) }
    }

    fun applyTrusted(peers: List<TrustedPeer>) {
        val byDeviceId = peers.associateBy { it.deviceId }
        byDeviceId.forEach { (deviceId, record) -> directory.of(deviceId).onTrusted(record) }
        directory.all
            .filterNot { it.peerDeviceId in byDeviceId }
            .forEach { it.onTrusted(null) }
    }

    fun applyPairing(pending: Set<DeviceId>) {
        pending.forEach { directory.of(it).onPairing(true) }
        directory.all
            .filterNot { it.peerDeviceId in pending }
            .forEach { it.onPairing(false) }
    }

    suspend fun applyPolicies(default: SyncPolicy, perPeer: Map<DeviceId, SyncPolicy>) {
        policies.update(default, perPeer)
        directory.all.forEach { it.onPolicy(policies.policyFor(it.peerDeviceId)) }
    }

    suspend fun run(session: PeerSession, origin: SessionOrigin) {
        val outcome = outcomeOf(session, origin)
        if (outcome is SessionOutcome.Pairing) settle(outcome.result)
    }

    override suspend fun claim(
        peer: PeerDescriptor,
        origin: SessionOrigin,
        session: PeerSession,
    ): SessionBinding? = directory.of(peer.deviceId).claim(peer, origin, session)

    fun reconnect(deviceId: DeviceId) {
        directory.find(deviceId)?.reconnect()
    }

    fun reconnectAll() {
        directory.all.forEach { it.reconnect() }
    }

    suspend fun forget(deviceId: DeviceId) {
        directory.remove(deviceId)?.close(CloseReason.UNPAIRED)
    }

    suspend fun shutdown() {
        directory.drain().forEach { it.close(CloseReason.SHUTDOWN) }
    }

    private suspend fun outcomeOf(session: PeerSession, origin: SessionOrigin): SessionOutcome? = try {
        runner.run(session, origin)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Exception) {
        null
    }

    private suspend fun settle(result: PairingResult) {
        val record = pairings.settle(result) ?: return
        directory.of(record.deviceId).onTrusted(record)
        if (record.isTrusted) reconnect(record.deviceId)
    }
}
