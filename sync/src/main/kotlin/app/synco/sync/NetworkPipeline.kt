package app.synco.sync

import app.synco.discovery.DiscoveryService
import app.synco.discovery.NetworkMonitor
import kotlinx.coroutines.CancellationException

internal class NetworkPipeline(
    private val network: NetworkMonitor,
    private val discovery: DiscoveryService,
    private val state: SyncStateHolder,
) {
    suspend fun run(registry: PeerSessionRegistry) {
        network.changes.collect { change ->
            if (change.carriesLocalNetwork) {
                state.clear(SyncProblem.NETWORK_UNAVAILABLE)
                restartDiscovery()
                registry.reconnectAll()
            } else {
                state.raise(SyncProblem.NETWORK_UNAVAILABLE)
            }
        }
    }

    private suspend fun restartDiscovery() {
        try {
            discovery.restart()
            state.clear(SyncProblem.DISCOVERY_UNAVAILABLE)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            state.raise(SyncProblem.DISCOVERY_UNAVAILABLE)
        }
    }
}
