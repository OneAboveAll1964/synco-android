package app.synco.sync

import app.synco.logging.SyncoLog
import app.synco.protocol.message.CloseReason
import app.synco.transport.PeerSession
import app.synco.transport.SessionEvent
import app.synco.transport.SessionOutcome
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal class PeerSessionRunner(private val hosts: SessionHosts) {

    suspend fun run(session: PeerSession, origin: SessionOrigin): SessionOutcome = coroutineScope {
        launch { pump(session, origin) }
        val outcome = session.run()
        SyncoLog.session.info("a ${origin.name.lowercase()} session finished as ${describe(outcome)}")
        if (outcome is SessionOutcome.Pairing) hosts.settlePairing(outcome.result)
        outcome
    }

    private fun describe(outcome: SessionOutcome): String = when (outcome) {
        is SessionOutcome.Pairing -> "pairing ${outcome.result}"
        is SessionOutcome.Ended -> "ended (${outcome.closeReason})"
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
