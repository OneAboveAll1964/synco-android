package app.synco.sync

import app.synco.discovery.DiscoveredPeer
import app.synco.transport.PeerSession

interface PeerDialer {
    suspend fun dial(peer: DiscoveredPeer): PeerSession
}
