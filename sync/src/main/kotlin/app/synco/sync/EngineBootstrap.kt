package app.synco.sync

import app.synco.discovery.AdvertisedIdentity
import app.synco.discovery.DiscoveryService
import app.synco.protocol.Platform
import app.synco.storage.IdentityStore
import app.synco.storage.SettingsStore
import app.synco.storage.TrustedPeerStore
import app.synco.transport.LocalDevice
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal class EngineBootstrap(
    private val identity: IdentityStore,
    private val settings: SettingsStore,
    private val trustedPeers: TrustedPeerStore,
    private val endpoints: SessionEndpointFactory,
    private val discovery: DiscoveryService,
    private val routers: ClipRouterFactory,
    private val pairings: PairingCoordinator,
    private val pipelines: EnginePipelines,
    private val transfers: TransferGateway,
    private val state: SyncStateHolder,
) {
    suspend fun launch(parent: CoroutineScope): EngineRuntime {
        settings.pruneUntrustedDirections()
        val local = localDevice()
        state.clear(SyncProblem.IDENTITY_UNREADABLE)
        val scope = childScope(parent)
        val endpoint = endpoints.create(local)
        val registry = PeerSessionRegistry(local.deviceId, routers, endpoint, state, scope, pairings)
        try {
            val port = endpoint.bind()
            state.setIdentity(DeviceIdentity.of(local))
            state.clear(SyncProblem.LISTENER_UNAVAILABLE)
            advertise(local, port)
            collect(scope, registry, endpoint)
            pipelines.launch(scope, registry)
            return EngineRuntime(scope, registry, endpoint, discovery, transfers)
        } catch (failure: Throwable) {
            scope.cancel()
            endpoint.close()
            throw failure
        }
    }

    private fun childScope(parent: CoroutineScope): CoroutineScope = CoroutineScope(
        parent.coroutineContext + SupervisorJob(parent.coroutineContext[Job]) + Dispatchers.Default,
    )

    private suspend fun localDevice(): LocalDevice = LocalDevice(
        identity = identity.identity(),
        displayName = settings.displayName.first(),
        platform = Platform.ANDROID,
    )

    private suspend fun advertise(local: LocalDevice, port: Int) {
        val advertised = AdvertisedIdentity(
            deviceId = local.deviceId,
            displayName = local.displayName,
            platform = local.platform,
            fingerprint = local.fingerprint,
        )
        try {
            discovery.start(port, advertised)
            state.clear(SyncProblem.DISCOVERY_UNAVAILABLE)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            state.raise(SyncProblem.DISCOVERY_UNAVAILABLE)
        }
    }

    private fun collect(
        scope: CoroutineScope,
        registry: PeerSessionRegistry,
        endpoint: SessionEndpoint,
    ) {
        scope.launch { discovery.peers.collect { registry.applyDiscovered(it) } }
        scope.launch { trustedPeers.peers.collect { registry.applyTrusted(it) } }
        scope.launch { registry.views.collect { state.publishPeers(it) } }
        scope.launch { transfers.progress.collect { state.recordTransfer(it) } }
        scope.launch {
            pairings.pending.collect { pending ->
                state.publishPendingPairings(pending)
                registry.applyPairing(pending.map { it.deviceId }.toSet())
            }
        }
        scope.launch {
            endpoint.accepted().collect { session ->
                scope.launch { registry.run(session, SessionOrigin.ACCEPTED) }
            }
        }
    }
}
