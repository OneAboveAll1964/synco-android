package app.synco.sync

import app.synco.transport.PeerSession
import kotlinx.coroutines.flow.Flow

interface SessionEndpoint : PeerDialer {

    suspend fun bind(): Int

    fun accepted(): Flow<PeerSession>

    suspend fun close()
}
