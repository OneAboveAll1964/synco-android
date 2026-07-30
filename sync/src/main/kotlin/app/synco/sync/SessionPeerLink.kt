package app.synco.sync

import app.synco.protocol.DeviceId
import app.synco.protocol.framing.BlobChunk
import app.synco.protocol.message.Envelope
import app.synco.transport.PeerSession

class SessionPeerLink(
    override val peerDeviceId: DeviceId,
    private val session: PeerSession,
) : PeerLink {

    override suspend fun send(envelope: Envelope) {
        session.send(envelope)
    }

    override suspend fun send(chunk: BlobChunk) {
        session.send(chunk)
    }
}
