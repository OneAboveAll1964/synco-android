package app.synco.sync

import app.synco.clipboard.ClipboardSnapshot
import app.synco.protocol.message.ClipRep

internal class StubBlobSender(private val delivers: Boolean = true) : BlobSender {

    val streamed = mutableListOf<ClipRep>()

    override suspend fun stream(
        snapshot: ClipboardSnapshot,
        rep: ClipRep,
        link: PeerLink,
        aborts: OutboundAborts,
    ): Boolean {
        streamed += rep
        return delivers
    }
}
