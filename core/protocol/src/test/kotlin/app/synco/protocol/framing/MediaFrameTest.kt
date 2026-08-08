package app.synco.protocol.framing

import app.synco.protocol.SyncoError
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaFrameTest {

    @Test
    fun `a media frame round-trips through encode and decode`() {
        val frame = MediaFrame(
            stream = 0,
            flags = MediaFrame.KEYFRAME or MediaFrame.LAST_FRAGMENT,
            seq = 4_294_000_111L,
            ptsMicros = 9_123_456_789L,
            data = byteArrayOf(0, 0, 0, 1, 103, 66, 42, 7),
        )

        val decoded = MediaFrame.decode(frame.encode())

        assertEquals(frame, decoded)
        assertEquals(0, decoded.stream)
        assertEquals(4_294_000_111L, decoded.seq)
        assertEquals(9_123_456_789L, decoded.ptsMicros)
        assertArrayEquals(frame.data, decoded.data)
    }

    @Test
    fun `the flag helpers read each bit`() {
        assertTrue(MediaFrame(0, MediaFrame.KEYFRAME, 1, 0, ByteArray(0)).isKeyframe)
        assertTrue(MediaFrame(0, MediaFrame.CONFIG, 1, 0, ByteArray(0)).isConfig)
        assertTrue(MediaFrame(0, MediaFrame.LAST_FRAGMENT, 1, 0, ByteArray(0)).isLastFragment)
        val plain = MediaFrame(0, 0, 1, 0, ByteArray(0))
        assertFalse(plain.isKeyframe)
        assertFalse(plain.isConfig)
        assertFalse(plain.isLastFragment)
    }

    @Test
    fun `a full sequence number survives the uint32 field`() {
        val frame = MediaFrame(0, 0, 0xFFFFFFFFL, 0, ByteArray(0))

        assertEquals(0xFFFFFFFFL, MediaFrame.decode(frame.encode()).seq)
    }

    @Test
    fun `an empty payload frame carries only its header`() {
        val frame = MediaFrame(1, MediaFrame.CONFIG, 7, 42, ByteArray(0))

        val encoded = frame.encode()

        assertEquals(MediaFrame.HEADER_BYTES, encoded.size)
        assertEquals(frame, MediaFrame.decode(encoded))
    }

    @Test(expected = SyncoError.Malformed::class)
    fun `a body shorter than the header is rejected`() {
        MediaFrame.decode(ByteArray(MediaFrame.HEADER_BYTES - 1))
    }

    @Test
    fun `the media frame kind survives a payload round-trip`() {
        val frame = MediaFrame(0, MediaFrame.KEYFRAME, 3, 5, byteArrayOf(1, 2, 3))

        val payload = FramePayload.decode(FramePayload.media(frame).encode())

        assertEquals(FrameKind.MEDIA, payload.kind)
        assertEquals(frame, MediaFrame.decode(payload.body))
    }
}
