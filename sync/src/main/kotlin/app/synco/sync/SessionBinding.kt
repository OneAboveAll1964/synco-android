package app.synco.sync

import app.synco.protocol.framing.BlobChunk
import app.synco.protocol.framing.MediaFrame
import app.synco.protocol.message.Caps
import app.synco.protocol.message.CloseReason
import app.synco.protocol.message.Envelope
import app.synco.protocol.message.PolicySync
import app.synco.transport.PeerSession

internal class SessionBinding(
    private val connection: PeerConnection,
    private val session: PeerSession,
    private val router: ClipRouter,
    private val media: RemoteMediaSink,
) {
    suspend fun receive(envelope: Envelope) {
        when (envelope) {
            is Caps -> connection.onPeerCaps(envelope)
            is PolicySync -> connection.onPeerPolicy(envelope)
            else -> router.receive(envelope)
        }
    }

    suspend fun receive(chunk: BlobChunk) {
        router.receive(chunk)
    }

    fun receive(frame: MediaFrame) {
        media.onMediaFrame(frame)
    }

    suspend fun release(reason: CloseReason?) {
        connection.release(session, reason)
    }
}
