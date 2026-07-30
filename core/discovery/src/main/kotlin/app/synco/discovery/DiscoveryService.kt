package app.synco.discovery

import kotlinx.coroutines.flow.StateFlow

interface DiscoveryService {

    val peers: StateFlow<List<DiscoveredPeer>>

    suspend fun start(port: Int, advertisedIdentity: AdvertisedIdentity)

    suspend fun stop()

    suspend fun restart()
}
