package app.synco.transport

import app.synco.crypto.HandshakeRole
import app.synco.protocol.framing.BlobChunk
import app.synco.protocol.message.CloseReason
import app.synco.protocol.message.Envelope

sealed interface SessionEvent {

    data class Established(val peer: PeerDescriptor, val role: HandshakeRole) : SessionEvent

    data class Received(val envelope: Envelope) : SessionEvent

    data class BlobReceived(val chunk: BlobChunk) : SessionEvent

    data class Terminated(val reason: CloseReason, val cause: Throwable? = null) : SessionEvent
}
