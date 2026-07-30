package app.synco.sync

import app.synco.protocol.framing.BlobChunk
import app.synco.protocol.message.Caps
import app.synco.protocol.message.CloseReason
import app.synco.protocol.message.Envelope
import app.synco.transport.PeerSession

internal class SessionBinding(
    private val connection: PeerConnection,
    private val session: PeerSession,
    private val router: ClipRouter,
) {
    suspend fun receive(envelope: Envelope) {
        if (envelope is Caps) connection.onPeerCaps(envelope) else router.receive(envelope)
    }

    suspend fun receive(chunk: BlobChunk) {
        router.receive(chunk)
    }

    suspend fun release(reason: CloseReason?) {
        connection.release(session, reason)
    }
}
