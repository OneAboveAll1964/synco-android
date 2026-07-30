package app.synco.transport

import app.synco.protocol.message.Bye
import app.synco.protocol.message.CloseReason
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.SendChannel
import java.util.concurrent.atomic.AtomicBoolean

internal class SessionTermination(
    private val frames: FramedConnection,
    private val events: SendChannel<SessionEvent>,
) {
    private val goodbyeSent = AtomicBoolean(false)

    suspend fun sendGoodbye(reason: CloseReason) {
        if (!goodbyeSent.compareAndSet(false, true)) return
        val failure = runCatching { frames.write(Bye.of(reason)) }.exceptionOrNull()
        if (failure is CancellationException) throw failure
    }

    suspend fun finish(outcome: SessionOutcome) {
        sendGoodbye(outcome.closeReason)
        frames.close()
        events.trySend(
            SessionEvent.Terminated(
                outcome.closeReason,
                (outcome as? SessionOutcome.Ended)?.cause,
            ),
        )
        events.close()
    }
}
