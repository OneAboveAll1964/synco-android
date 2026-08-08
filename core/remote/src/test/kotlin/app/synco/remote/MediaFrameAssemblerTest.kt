package app.synco.remote

import app.synco.protocol.framing.MediaFrame
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaFrameAssemblerTest {

    private fun frame(seq: Long, data: ByteArray, flags: Int, pts: Long = 0) =
        MediaFrame(stream = 0, flags = flags, seq = seq, ptsMicros = pts, data = data)

    @Test
    fun `a single last-fragment frame yields its access unit immediately`() {
        val assembler = MediaFrameAssembler()

        val unit = assembler.accept(
            frame(1, byteArrayOf(9, 8, 7), MediaFrame.KEYFRAME or MediaFrame.LAST_FRAGMENT, pts = 42),
        )

        assertArrayEquals(byteArrayOf(9, 8, 7), unit?.data)
        assertEquals(42L, unit?.ptsMicros)
        assertTrue(unit!!.isKeyframe)
    }

    @Test
    fun `fragments accumulate until the last one, then reassemble in order`() {
        val assembler = MediaFrameAssembler()

        assertNull(assembler.accept(frame(5, byteArrayOf(1, 2), MediaFrame.KEYFRAME)))
        assertNull(assembler.accept(frame(5, byteArrayOf(3, 4), 0)))
        val unit = assembler.accept(frame(5, byteArrayOf(5), MediaFrame.LAST_FRAGMENT))

        assertArrayEquals(byteArrayOf(1, 2, 3, 4, 5), unit?.data)
        assertTrue(unit!!.isKeyframe)
    }

    @Test
    fun `a new sequence discards an unfinished previous one`() {
        val assembler = MediaFrameAssembler()

        assembler.accept(frame(1, byteArrayOf(1, 2), 0))
        val unit = assembler.accept(frame(2, byteArrayOf(9), MediaFrame.LAST_FRAGMENT))

        assertArrayEquals(byteArrayOf(9), unit?.data)
    }

    @Test
    fun `a config access unit is flagged`() {
        val assembler = MediaFrameAssembler()

        val unit = assembler.accept(frame(0, byteArrayOf(0, 0, 0, 1, 103), MediaFrame.CONFIG or MediaFrame.LAST_FRAGMENT))

        assertTrue(unit!!.isConfig)
    }
}
