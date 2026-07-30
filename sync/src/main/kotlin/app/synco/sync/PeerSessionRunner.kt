package app.synco.sync

import app.synco.protocol.message.CloseReason
import app.synco.transport.PeerSession
import app.synco.transport.SessionEvent
import app.synco.transport.SessionOutcome
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal class PeerSessionRunner(private val hosts: SessionHosts) {

    suspend fun run(session: PeerSession, origin: SessionOrigin): SessionOutcome = coroutineScope {
        launch { pump(session, origin) }
        session.run()
    }

    private suspend fun pump(session: PeerSession, origin: SessionOrigin) {
        var binding: SessionBinding? = null
        session.events.collect { event ->
            when (event) {
                is SessionEvent.Established -> {
                    binding = hosts.claim(event.peer, origin, session)
                    if (binding == null) quietly { session.close(CloseReason.DUPLICATE_SESSION) }
                }
                is SessionEvent.Received -> binding?.receive(event.envelope)
                is SessionEvent.BlobReceived -> binding?.receive(event.chunk)
                is SessionEvent.Terminated -> binding?.release(event.reason)
            }
        }
    }
}
