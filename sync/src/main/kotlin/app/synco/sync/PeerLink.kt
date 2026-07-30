package app.synco.sync

import app.synco.protocol.DeviceId
import app.synco.protocol.framing.BlobChunk
import app.synco.protocol.message.Envelope

interface PeerLink {
    val peerDeviceId: DeviceId

    suspend fun send(envelope: Envelope)

    suspend fun send(chunk: BlobChunk)
}
