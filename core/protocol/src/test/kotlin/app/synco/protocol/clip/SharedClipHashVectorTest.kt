package app.synco.protocol.clip

import app.synco.protocol.encoding.Hex
import app.synco.protocol.message.ClipRep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SharedClipHashVectorTest {

    @Test
    fun `hashes the shared every-kind representation list`() {
        assertEquals(ALL_KINDS_DIGEST, ClipHash.compute(allKinds()))
        assertEquals(ALL_KINDS_CANONICAL_BYTES, ClipHash.canonicalBytes(allKinds()).size)
    }

    @Test
    fun `hashes the shared text and url pair`() {
        assertEquals(TEXT_URL_PAIR_DIGEST, ClipHash.compute(textUrlPair()))
    }

    @Test
    fun `escapes separators so content cannot forge a representation boundary`() {
        assertEquals(SEPARATOR_TEXT_DIGEST, ClipHash.compute(separatorText()))
        assertNotEquals(ClipHash.compute(textUrlPair()), ClipHash.compute(separatorText()))
        assertEquals(
            SEPARATOR_TEXT_CANONICAL,
            Hex.encodeLower(ClipHash.canonicalBytes(separatorText())),
        )
    }

    @Test
    fun `escapes the escape byte itself`() {
        assertEquals(ESCAPE_TEXT_DIGEST, ClipHash.compute(escapeText()))
        assertEquals(ESCAPE_TEXT_CANONICAL, Hex.encodeLower(ClipHash.canonicalBytes(escapeText())))
    }

    @Test
    fun `escapes a separator inside a file name`() {
        assertEquals(FILE_SEPARATOR_NAME_DIGEST, ClipHash.compute(fileSeparatorName()))
    }

    @Test
    fun `hashes the shared empty representation list`() {
        assertEquals(EMPTY_DIGEST, ClipHash.compute(emptyList()))
    }

    private fun allKinds(): List<ClipRep> = listOf(
        ClipRep.Text("hello"),
        ClipRep.Html("<b>hello</b>"),
        ClipRep.Rtf(RTF_BASE64),
        ClipRep.Url("https://example.com/a?b=c", title = "Example"),
        ClipRep.Image("image/png", "shot.png", 91_234L, IMAGE_SHA256, TRANSFER_ID_A),
        ClipRep.File("text/plain", "notes.txt", 12L, FILE_SHA256, TRANSFER_ID_B, rel = "docs/notes.txt"),
    )

    private fun textUrlPair(): List<ClipRep> = listOf(
        ClipRep.Text("hello"),
        ClipRep.Url("https://example.com"),
    )

    private fun separatorText(): List<ClipRep> =
        listOf(ClipRep.Text("hello${RECORD_SEPARATOR}url${UNIT_SEPARATOR}https://example.com"))

    private fun escapeText(): List<ClipRep> = listOf(ClipRep.Text("a${ESCAPE}b"))

    private fun fileSeparatorName(): List<ClipRep> = listOf(
        ClipRep.File("text/plain", "a${UNIT_SEPARATOR}b", 1L, "0f1e2d", TRANSFER_ID_A),
    )

    private companion object {
        const val UNIT_SEPARATOR = '\u001F'
        const val RECORD_SEPARATOR = '\u001E'
        const val ESCAPE = '\u001D'

        const val TRANSFER_ID_A = "11112222-3333-4444-5555-666677778888"
        const val TRANSFER_ID_B = "3f2a1b0c-4d5e-6f70-8192-a3b4c5d6e7f8"
        const val RTF_BASE64 = "e1xydGYxfQ=="
        const val IMAGE_SHA256 = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08"
        const val FILE_SHA256 = "2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae"

        const val ALL_KINDS_CANONICAL_BYTES = 222
        const val ALL_KINDS_DIGEST =
            "7f94c6d298dcb84c889d004037ba6d1efc3048bdb28637163af92a86183bf81c"
        const val TEXT_URL_PAIR_DIGEST =
            "ca36eabccedc7d0156bb0f77a282401f22d07741ed132b9ddd1721ea01bf9a03"
        const val SEPARATOR_TEXT_DIGEST =
            "f9beec0628125ed0edd62ed2fe372cb29e1eab255935c7ffd6905831458d6e1b"
        const val SEPARATOR_TEXT_CANONICAL =
            "746578741f68656c6c6f1d1e75726c1d1f68747470733a2f2f6578616d706c652e636f6d1e"
        const val ESCAPE_TEXT_DIGEST =
            "57940913c675610bcbb7055e74c51933eedade26fafbcb8a5eb85b603440b114"
        const val ESCAPE_TEXT_CANONICAL = "746578741f611d1d621e"
        const val FILE_SEPARATOR_NAME_DIGEST =
            "03360c1747ee867ec7e4efe89d409d027b40294f02eaabcff0bd5428c1ed832e"
        const val EMPTY_DIGEST =
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    }
}
