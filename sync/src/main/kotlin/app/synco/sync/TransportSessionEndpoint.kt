package app.synco.sync

import app.synco.discovery.DiscoveredPeer
import app.synco.transport.PeerSession
import app.synco.transport.SyncoServer
import app.synco.transport.SyncoSocketFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TransportSessionEndpoint(
    private val sockets: SyncoSocketFactory,
    private val sessions: PeerSessionFactory,
) : SessionEndpoint {

    @Volatile
    private var server: SyncoServer? = null

    override suspend fun bind(): Int {
        close()
        val bound = SyncoServer.bind(sockets)
        server = bound
        return bound.port
    }

    override fun accepted(): Flow<PeerSession> = flow {
        val bound = server ?: return@flow
        bound.connections.collect { connection -> emit(sessions.accept(connection)) }
    }

    override suspend fun dial(peer: DiscoveredPeer): PeerSession = sessions.dial(peer.host, peer.port)

    override suspend fun close() {
        server?.close()
        server = null
    }
}
