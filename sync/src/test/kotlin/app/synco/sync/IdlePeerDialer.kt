package app.synco.sync

import app.synco.discovery.DiscoveredPeer
import app.synco.transport.PeerSession
import kotlinx.coroutines.awaitCancellation

internal class IdlePeerDialer : PeerDialer {

    val attempts = mutableListOf<DiscoveredPeer>()

    override suspend fun dial(peer: DiscoveredPeer): PeerSession {
        attempts += peer
        awaitCancellation()
    }
}
