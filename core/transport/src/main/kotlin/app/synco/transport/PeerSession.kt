package app.synco.transport

import app.synco.protocol.ProtocolConstants
import app.synco.protocol.SyncoError
import app.synco.protocol.framing.BlobChunk
import app.synco.protocol.message.CloseReason
import app.synco.protocol.message.Envelope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class PeerSession internal constructor(
    local: LocalDevice,
    private val frames: FramedConnection,
    trustedPeers: TrustedPeers,
    pairingApproval: PairingApproval,
) {
    constructor(
        local: LocalDevice,
        connection: TransportConnection,
        trustedPeers: TrustedPeers,
        pairingApproval: PairingApproval,
    ) : this(local, connection.frames, trustedPeers, pairingApproval)

    private val handshake = SessionHandshake(local, frames, trustedPeers)
    private val pairing = PairingExchange(local, frames, pairingApproval)
    private val sink = Channel<SessionEvent>(Channel.UNLIMITED)
    private val receiveLoop = SessionReceiveLoop(frames, sink)
    private val termination = SessionTermination(frames, sink)
    private val establishedPeer = CompletableDeferred<PeerDescriptor>()

    @Volatile
    private var descriptor: PeerDescriptor? = null

    @Volatile
    private var requestedReason: CloseReason? = null

    val events: Flow<SessionEvent> = sink.receiveAsFlow()

    val peer: PeerDescriptor? get() = descriptor

    suspend fun run(): SessionOutcome = try {
        finishing(drive())
    } catch (cancellation: CancellationException) {
        finishing(endedBy(cancellation))
        throw cancellation
    } catch (failure: Throwable) {
        finishing(endedBy(failure))
    }

    suspend fun send(envelope: Envelope) {
        establishedPeer.await()
        frames.write(envelope)
    }

    suspend fun send(chunk: BlobChunk) {
        establishedPeer.await()
        frames.write(chunk)
    }

    suspend fun close(reason: CloseReason) {
        requestedReason = reason
        termination.sendGoodbye(reason)
        frames.close()
    }

    private suspend fun drive(): SessionOutcome {
        val peerHello = awaitHandshakeStep { handshake.exchangeHello() }
        val peerStaticKey = handshake.trustedKeyFor(peerHello)
            ?: return SessionOutcome.Pairing(pairing.run(peerHello))
        val outcome = awaitHandshakeStep { handshake.authenticate(peerHello, peerStaticKey) }
        if (outcome is HandshakeOutcome.Unpaired) {
            return SessionOutcome.Pairing(pairing.run(peerHello, outcome.pendingRequest))
        }
        val session = (outcome as HandshakeOutcome.Established).session
        frames.upgrade(session.ciphers)
        descriptor = session.peer
        sink.trySend(SessionEvent.Established(session.peer, session.role))
        establishedPeer.complete(session.peer)
        return SessionOutcome.Ended(session.peer.deviceId, runEstablished())
    }

    private suspend fun runEstablished(): CloseReason = coroutineScope {
        val heartbeat = launch { SessionHeartbeat(frames, frames.activity).run() }
        val watchdog = launch { ReadTimeoutWatchdog(frames.activity).run() }
        try {
            receiveLoop.run()
        } finally {
            heartbeat.cancel()
            watchdog.cancel()
        }
    }

    private suspend fun <T : Any> awaitHandshakeStep(step: suspend () -> T): T =
        withTimeoutOrNull(ProtocolConstants.READ_TIMEOUT_MILLIS) { step() }
            ?: throw SyncoError.Timeout(
                "the handshake stalled for ${ProtocolConstants.READ_TIMEOUT_MILLIS}ms",
            )

    private fun endedBy(failure: Throwable): SessionOutcome = SessionOutcome.Ended(
        peerDeviceId = descriptor?.deviceId,
        closeReason = requestedReason ?: CloseReasons.of(failure),
        cause = failure,
    )

    private suspend fun finishing(outcome: SessionOutcome): SessionOutcome =
        withContext(NonCancellable) {
            termination.finish(outcome)
            establishedPeer.completeExceptionally(TransportError.NotEstablished())
            outcome
        }
}
