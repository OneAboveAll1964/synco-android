package app.synco.sync

import app.synco.protocol.message.CloseReason
import app.synco.transport.PeerSession

internal class LiveSession(
    val origin: SessionOrigin,
    val session: PeerSession,
    val link: PeerLink,
    val router: ClipRouter,
) {
    suspend fun shutDown(reason: CloseReason) {
        router.cancel()
        quietly { session.close(reason) }
    }
}
