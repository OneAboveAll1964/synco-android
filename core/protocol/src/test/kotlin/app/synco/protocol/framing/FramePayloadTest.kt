package app.synco.protocol.framing

import app.synco.protocol.SyncoError
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.UUID

class FramePayloadTest {

    @Test
    fun `numbers the control kind one and the blob kind two`() {
        assertEquals(0x01.toByte(), FrameKind.CONTROL.code)
        assertEquals(0x02.toByte(), FrameKind.BLOB.code)
        assertEquals(0x03.toByte(), FrameKind.MEDIA.code)
        assertEquals(FrameKind.CONTROL, FrameKind.fromCode(0x01))
        assertEquals(FrameKind.BLOB, FrameKind.fromCode(0x02))
        assertEquals(FrameKind.MEDIA, FrameKind.fromCode(0x03))
        assertNull(FrameKind.fromCode(0x04))
    }

    @Test
    fun `prefixes the body with the kind byte`() {
        val payload = FramePayload.control("hi".toByteArray())
        assertArrayEquals(byteArrayOf(0x01, 'h'.code.toByte(), 'i'.code.toByte()), payload.encode())
        assertEquals(payload, FramePayload.decode(payload.encode()))
    }

    @Test
    fun `wraps a blob chunk`() {
        val chunk = BlobChunk(UUID.randomUUID(), 512L, ByteArray(64) { 0x09 })
        val payload = FramePayload.blob(chunk)
        val decoded = FramePayload.decode(payload.encode())
        assertEquals(FrameKind.BLOB, decoded.kind)
        assertEquals(chunk, BlobChunk.decode(decoded.body))
    }

    @Test
    fun `rejects an empty payload and an unknown kind`() {
        assertThrows(SyncoError.Malformed::class.java) { FramePayload.decode(ByteArray(0)) }
        assertThrows(SyncoError.Malformed::class.java) { FramePayload.decode(byteArrayOf(0x7F)) }
    }
}
