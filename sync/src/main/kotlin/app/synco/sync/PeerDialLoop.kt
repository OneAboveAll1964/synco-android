package app.synco.sync

import app.synco.discovery.DiscoveredPeer
import app.synco.protocol.message.CloseReason
import app.synco.transport.Backoff
import app.synco.transport.PeerSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withTimeoutOrNull

internal class PeerDialLoop(
    private val connection: PeerConnection,
    private val facts: PeerFacts,
    private val slot: PeerSessionSlot,
    private val dialer: PeerDialer,
    private val runner: PeerSessionRunner,
    private val backoff: Backoff,
) {
    private val nudges = Channel<Unit>(Channel.CONFLATED)

    @Volatile
    var dialing = false
        private set

    fun nudge() {
        nudges.trySend(Unit)
    }

    suspend fun run() {
        while (true) {
            val target = awaitTarget()
            dialing = true
            facts.setStatus(PeerConnectionStatus.CONNECTING)
            val session = open(target)
            if (session == null) facts.setStatus(PeerConnectionStatus.RETRYING) else drive(session)
            awaitRetry()
        }
    }

    private suspend fun awaitTarget(): DiscoveredPeer {
        dialing = false
        facts.setStatus(facts.idleStatus())
        return facts.dialTargets(slot.live).first()
    }

    private suspend fun open(target: DiscoveredPeer): PeerSession? = try {
        dialer.dial(target)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Exception) {
        null
    }

    private suspend fun drive(session: PeerSession) {
        try {
            runner.run(session, SessionOrigin.DIALED)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            connection.release(session, CloseReason.SHUTDOWN)
        }
    }

    private suspend fun awaitRetry() {
        val delayMillis = backoff.nextDelayMillis()
        if (delayMillis <= 0) return
        withTimeoutOrNull(delayMillis) {
            merge(facts.disappearances(), nudges.receiveAsFlow()).first()
        }
    }
}
