package app.synco.transport

import app.synco.crypto.SessionCipherPair
import app.synco.protocol.ProtocolConstants
import app.synco.protocol.framing.BlobChunk
import app.synco.protocol.framing.FrameCodec
import app.synco.protocol.framing.FramePayload
import app.synco.protocol.message.Envelope
import app.synco.protocol.message.EnvelopeCodec
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.cancel
import io.ktor.utils.io.readFully
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.EOFException
import java.io.IOException

class FramedConnection internal constructor(
    private val input: ByteReadChannel,
    private val output: ByteWriteChannel,
    internal val activity: ConnectionActivity = ConnectionActivity(),
    private val onClose: () -> Unit = {},
) {
    private val readMutex = Mutex()
    private val writeMutex = Mutex()
    private val lengthPrefix = ByteArray(ProtocolConstants.FRAME_LENGTH_PREFIX_BYTES)

    @Volatile
    private var ciphers: SessionCipherPair? = null

    val isEncrypted: Boolean get() = ciphers != null

    fun upgrade(ciphers: SessionCipherPair) {
        this.ciphers = ciphers
    }

    suspend fun write(envelope: Envelope) {
        write(FramePayload.control(EnvelopeCodec.encodeToBytes(envelope)))
    }

    suspend fun write(chunk: BlobChunk) {
        write(FramePayload.blob(chunk))
    }

    suspend fun write(payload: FramePayload): Unit = writeMutex.withLock {
        val plaintext = payload.encode()
        val body = ciphers?.seal(plaintext) ?: plaintext
        val length = FrameCodec.validateLength(body.size.toLong())
        translatingFailures {
            output.writeFully(FrameCodec.encodeLengthPrefix(length))
            output.writeFully(body)
            output.flush()
        }
        activity.recordWrite()
    }

    suspend fun read(): FramePayload = readMutex.withLock {
        translatingFailures { input.readFully(lengthPrefix) }
        val length = FrameCodec.validateLength(FrameCodec.readLengthPrefix(lengthPrefix))
        val body = ByteArray(length)
        translatingFailures { input.readFully(body) }
        activity.recordRead()
        FramePayload.decode(ciphers?.open(body) ?: body)
    }

    suspend fun close() {
        val failure = runCatching { output.flushAndClose() }.exceptionOrNull()
        input.cancel()
        onClose()
        if (failure is CancellationException) throw failure
    }

    private inline fun <T> translatingFailures(block: () -> T): T = try {
        block()
    } catch (endOfStream: EOFException) {
        throw TransportError.PeerClosed(endOfStream)
    } catch (failure: IOException) {
        throw TransportError.LinkFailed("the link failed: ${failure.message}", failure)
    }
}
