package app.synco.protocol.framing

import app.synco.protocol.ProtocolConstants
import app.synco.protocol.SyncoError
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class FrameCodecTest {

    @Test
    fun `writes a four byte big endian length prefix`() {
        val frame = FrameCodec.encode(ByteArray(0x0102_03) { 0x5A })
        assertArrayEquals(byteArrayOf(0x00, 0x01, 0x02, 0x03), frame.copyOf(4))
        assertEquals(0x0102_03L, FrameCodec.readLengthPrefix(frame))
    }

    @Test
    fun `round trips a frame through the decoder`() {
        val payload = ByteArray(1024) { (it % 251).toByte() }
        val decoder = FrameDecoder()
        decoder.feed(FrameCodec.encode(payload))
        assertArrayEquals(payload, decoder.next())
        assertNull(decoder.next())
    }

    @Test
    fun `decodes a frame fed one byte at a time`() {
        val payload = "the quick brown fox".toByteArray(Charsets.UTF_8)
        val frame = FrameCodec.encode(payload)
        val decoder = FrameDecoder()
        for (index in frame.indices) {
            decoder.feed(frame, index, 1)
            if (index < frame.lastIndex) assertNull(decoder.next())
        }
        assertArrayEquals(payload, decoder.next())
        assertEquals(0, decoder.bufferedBytes)
    }

    @Test
    fun `decodes several frames from one chunk and keeps a partial tail`() {
        val first = byteArrayOf(1, 2, 3)
        val second = byteArrayOf(4, 5)
        val stream = FrameCodec.encode(first) + FrameCodec.encode(second) + byteArrayOf(0x00, 0x00)
        val decoder = FrameDecoder()
        decoder.feed(stream)
        assertArrayEquals(first, decoder.next())
        assertArrayEquals(second, decoder.next())
        assertNull(decoder.next())
        assertEquals(2, decoder.bufferedBytes)
    }

    @Test
    fun `rejects an announced length above the maximum`() {
        val decoder = FrameDecoder()
        decoder.feed(FrameCodec.encodeLengthPrefix(ProtocolConstants.MAX_FRAME_PAYLOAD_BYTES + 1))
        val error = assertThrows(SyncoError.FrameTooLarge::class.java) { decoder.next() }
        assertEquals(ProtocolConstants.MAX_FRAME_PAYLOAD_BYTES + 1L, error.length)
    }

    @Test
    fun `rejects an announced length of zero`() {
        val decoder = FrameDecoder()
        decoder.feed(byteArrayOf(0x00, 0x00, 0x00, 0x00))
        assertThrows(SyncoError.Malformed::class.java) { decoder.next() }
    }

    @Test
    fun `rejects an announced length that overflows a signed int`() {
        val decoder = FrameDecoder()
        decoder.feed(byteArrayOf(-1, -1, -1, -1))
        val error = assertThrows(SyncoError.FrameTooLarge::class.java) { decoder.next() }
        assertEquals(0xFFFF_FFFFL, error.length)
    }

    @Test
    fun `refuses to encode an empty or oversized payload`() {
        assertThrows(SyncoError.Malformed::class.java) { FrameCodec.encode(ByteArray(0)) }
        assertThrows(SyncoError.FrameTooLarge::class.java) {
            FrameCodec.encode(ByteArray(ProtocolConstants.MAX_FRAME_PAYLOAD_BYTES + 1))
        }
    }

    @Test
    fun `accepts a payload of exactly the maximum size`() {
        val payload = ByteArray(ProtocolConstants.MAX_FRAME_PAYLOAD_BYTES) { 0x11 }
        val decoder = FrameDecoder()
        decoder.feed(FrameCodec.encode(payload))
        assertTrue(payload.contentEquals(decoder.next()))
    }
}
