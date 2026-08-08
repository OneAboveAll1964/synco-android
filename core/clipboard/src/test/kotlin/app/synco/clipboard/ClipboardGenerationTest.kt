package app.synco.clipboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardGenerationTest {

    @Test
    fun `a clip we applied stays known until the clipboard changes, however long it takes`() {
        val generation = ClipboardGeneration(clock = { 0L })
        generation.applied(setOf("mac-clip"), atStampMillis = 5_000L)

        assertTrue(generation.isKnown("any-read-back-variant", atStampMillis = 5_000L))
        assertTrue(generation.isKnown("mac-clip", atStampMillis = 5_000L))
    }

    @Test
    fun `a copy with a fresh stamp is new even when the text matches`() {
        val generation = ClipboardGeneration(clock = { 0L })
        generation.applied(setOf("hello"), atStampMillis = 5_000L)

        assertFalse(generation.isKnown("hello", atStampMillis = 9_000L))
    }

    @Test
    fun `a captured clip read again is known`() {
        val generation = ClipboardGeneration(clock = { 0L })
        generation.captured("copy", atStampMillis = 5_000L)

        assertTrue(generation.isKnown("copy", atStampMillis = 5_000L))
        assertFalse(generation.isKnown("copy", atStampMillis = 6_000L))
    }

    @Test
    fun `without stamps the hash is known only briefly`() {
        var now = 0L
        val generation = ClipboardGeneration(clock = { now })
        generation.applied(setOf("echo-a", "echo-b"), atStampMillis = null)

        assertTrue(generation.isKnown("echo-b", atStampMillis = null))
        now += 60_000
        assertFalse(generation.isKnown("echo-b", atStampMillis = null))
    }

    @Test
    fun `an unrelated hash without stamps is never known`() {
        val generation = ClipboardGeneration(clock = { 0L })
        generation.applied(setOf("echo"), atStampMillis = null)

        assertFalse(generation.isKnown("user-copy", atStampMillis = null))
    }
}
