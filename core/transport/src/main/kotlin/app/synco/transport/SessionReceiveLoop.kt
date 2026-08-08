package app.synco.transport

import app.synco.protocol.framing.BlobChunk
import app.synco.protocol.framing.FrameKind
import app.synco.protocol.framing.MediaFrame
import app.synco.protocol.message.Bye
import app.synco.protocol.message.CloseReason
import app.synco.protocol.message.Envelope
import app.synco.protocol.message.EnvelopeCodec
import app.synco.protocol.message.Ping
import app.synco.protocol.message.Pong
import kotlinx.coroutines.channels.SendChannel

internal class SessionReceiveLoop(
    private val frames: FramedConnection,
    private val events: SendChannel<SessionEvent>,
) {
    suspend fun run(): CloseReason {
        while (true) {
            val payload = frames.read()
            val closing = when (payload.kind) {
                FrameKind.CONTROL -> handle(EnvelopeCodec.decode(payload.body))
                FrameKind.BLOB -> emitBlob(payload.body)
                FrameKind.MEDIA -> emitMedia(payload.body)
            }
            if (closing != null) return closing
        }
    }

    private suspend fun handle(envelope: Envelope): CloseReason? = when (envelope) {
        is Ping -> {
            frames.write(Pong(envelope.sequence))
            null
        }
        is Pong -> null
        is Bye -> envelope.closeReason ?: CloseReason.SHUTDOWN
        else -> {
            events.send(SessionEvent.Received(envelope))
            null
        }
    }

    private suspend fun emitBlob(body: ByteArray): CloseReason? {
        events.send(SessionEvent.BlobReceived(BlobChunk.decode(body)))
        return null
    }

    private suspend fun emitMedia(body: ByteArray): CloseReason? {
        events.send(SessionEvent.MediaReceived(MediaFrame.decode(body)))
        return null
    }
}
