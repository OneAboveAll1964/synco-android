package app.synco.transport

import app.synco.protocol.SyncoError
import app.synco.protocol.framing.FrameKind
import app.synco.protocol.message.Bye
import app.synco.protocol.message.ControlMessage
import app.synco.protocol.message.EnvelopeCodec
import app.synco.protocol.message.UnknownMessage

internal object ControlFrames {

    suspend fun read(frames: FramedConnection): ControlMessage {
        val payload = frames.read()
        if (payload.kind != FrameKind.CONTROL) {
            throw SyncoError.Malformed("expected a control frame, received ${payload.kind}")
        }
        val envelope = EnvelopeCodec.decode(payload.body)
        if (envelope is UnknownMessage) {
            throw SyncoError.Malformed(
                "unexpected '${envelope.type}' message before the session was established",
            )
        }
        if (envelope is Bye) throw TransportError.PeerGoodbye(envelope.closeReason)
        return envelope as ControlMessage
    }

    fun unexpected(expected: String, actual: ControlMessage): SyncoError = SyncoError.Malformed(
        "expected a $expected message, received ${actual::class.simpleName}",
    )
}
