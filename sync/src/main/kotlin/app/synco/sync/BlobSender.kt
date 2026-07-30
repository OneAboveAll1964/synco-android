package app.synco.sync

import app.synco.clipboard.ClipboardSnapshot
import app.synco.protocol.message.ClipRep

interface BlobSender {

    suspend fun stream(
        snapshot: ClipboardSnapshot,
        rep: ClipRep,
        link: PeerLink,
        aborts: OutboundAborts,
    ): Boolean
}
