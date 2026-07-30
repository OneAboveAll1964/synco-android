package app.synco.sync

import app.synco.protocol.DeviceId
import app.synco.protocol.framing.BlobChunk
import app.synco.protocol.message.Ack
import app.synco.protocol.message.Clip
import app.synco.protocol.message.Envelope
import app.synco.protocol.message.TransferAbort
import app.synco.protocol.message.TransferStart

internal class RecordingPeerLink(
    override val peerDeviceId: DeviceId = ClipFixtures.PEER,
) : PeerLink {

    val envelopes = mutableListOf<Envelope>()

    val chunks = mutableListOf<BlobChunk>()

    val clips: List<Clip> get() = envelopes.filterIsInstance<Clip>()

    val acks: List<Ack> get() = envelopes.filterIsInstance<Ack>()

    val starts: List<TransferStart> get() = envelopes.filterIsInstance<TransferStart>()

    val aborts: List<TransferAbort> get() = envelopes.filterIsInstance<TransferAbort>()

    override suspend fun send(envelope: Envelope) {
        envelopes += envelope
    }

    override suspend fun send(chunk: BlobChunk) {
        chunks += chunk
    }
}
