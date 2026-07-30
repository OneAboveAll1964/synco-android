package app.synco.transport

import app.synco.protocol.HandshakeConstants
import app.synco.protocol.ProtocolConstants
import app.synco.protocol.SyncoError
import app.synco.protocol.framing.BlobChunk
import app.synco.protocol.framing.FrameCodec
import app.synco.protocol.framing.FrameKind
import app.synco.protocol.framing.FramePayload
import app.synco.protocol.message.EnvelopeCodec
import app.synco.protocol.message.Ping
import io.ktor.utils.io.readFully
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class FramedConnectionTest {

    @Test
    fun `round trips a control frame in plaintext`() = runTest {
        val link = TestLink()
        link.left.write(Ping(7))
        val payload = link.right.read()
        assertEquals(FrameKind.CONTROL, payload.kind)
        assertEquals(Ping(7), EnvelopeCodec.decode(payload.body))
        assertFalse(link.right.isEncrypted)
    }

    @Test
    fun `round trips a blob chunk in plaintext`() = runTest {
        val link = TestLink()
        val chunk = BlobChunk(UUID.randomUUID(), 262_144L, ByteArray(4096) { it.toByte() })
        link.left.write(chunk)
        val payload = link.right.read()
        assertEquals(FrameKind.BLOB, payload.kind)
        assertEquals(chunk, BlobChunk.decode(payload.body))
    }

    @Test
    fun `round trips frames in both directions once upgraded`() = runTest {
        val alice = SessionFixtures.device("Alice")
        val bob = SessionFixtures.device("Bob")
        val (forAlice, forBob) = SessionFixtures.sessionCiphers(alice, bob)
        val link = TestLink()
        link.left.upgrade(forAlice)
        link.right.upgrade(forBob)

        val chunk = BlobChunk(UUID.randomUUID(), 0L, ByteArray(64) { 0x2A })
        link.left.write(chunk)
        assertEquals(chunk, BlobChunk.decode(link.right.read().body))
        link.right.write(Ping(1))
        assertEquals(Ping(1), EnvelopeCodec.decode(link.left.read().body))
        assertTrue(link.left.isEncrypted)
    }

    @Test
    fun `adds the aead tag to the wire length once upgraded`() = runTest {
        val alice = SessionFixtures.device("Alice")
        val bob = SessionFixtures.device("Bob")
        val (forAlice, _) = SessionFixtures.sessionCiphers(alice, bob)
        val link = TestLink()
        link.left.upgrade(forAlice)
        link.left.write(Ping(3))

        val plaintextSize = FramePayload.control(EnvelopeCodec.encodeToBytes(Ping(3))).encode().size
        val prefix = ByteArray(ProtocolConstants.FRAME_LENGTH_PREFIX_BYTES)
        link.wireFromLeft.readFully(prefix)
        assertEquals(
            (plaintextSize + HandshakeConstants.AEAD_TAG_BYTES).toLong(),
            FrameCodec.readLengthPrefix(prefix),
        )
    }

    @Test
    fun `refuses a payload larger than the maximum frame`() = runTest {
        val link = TestLink()
        val oversized = FramePayload.control(ByteArray(ProtocolConstants.MAX_FRAME_PAYLOAD_BYTES))
        val failure = runCatching { link.left.write(oversized) }.exceptionOrNull()
        assertTrue(failure is SyncoError.FrameTooLarge)
    }

    @Test
    fun `reports a closed peer when the link ends`() = runTest {
        val link = TestLink()
        link.left.close()
        val failure = runCatching { link.right.read() }.exceptionOrNull()
        assertTrue(failure is TransportError.PeerClosed)
    }
}
