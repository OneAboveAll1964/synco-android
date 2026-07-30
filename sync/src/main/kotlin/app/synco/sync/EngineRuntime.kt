package app.synco.sync

import app.synco.discovery.DiscoveryService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

internal class EngineRuntime(
    private val scope: CoroutineScope,
    val registry: PeerSessionRegistry,
    private val endpoint: SessionEndpoint,
    private val discovery: DiscoveryService,
    private val transfers: TransferGateway,
) {
    suspend fun restartDiscovery() {
        quietly { discovery.restart() }
    }

    suspend fun shutdown() {
        registry.shutdown()
        quietly { discovery.stop() }
        quietly { endpoint.close() }
        transfers.shutdown()
        scope.cancel()
    }
}
