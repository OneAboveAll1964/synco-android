package app.synco.protocol.clip

import app.synco.protocol.ProtocolConstants
import app.synco.protocol.message.ClipRep
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ClipHashTest {

    @Test
    fun `hashes a single text representation`() {
        assertEquals(
            "a62bae66362ed98d8826886c3b955c939e1f499f78553f774b52a9cedfeda24e",
            ClipHash.compute(listOf(ClipRep.Text("hello"))),
        )
    }

    @Test
    fun `hashes an image representation over its digest only`() {
        assertEquals(
            "bf2134120bdd99c89b2762390a61b9d54501d02dbc5c0f086782e7ab1b9db71d",
            ClipHash.compute(listOf(ClipRep.Image("image/png", "shot.png", 91_234L, "aabbcc", TRANSFER_ID))),
        )
    }

    @Test
    fun `hashes an rtf representation over its decoded bytes`() {
        assertEquals(
            "141669dc797567ad556a73fbe5f49527be10e45fbab9238ebcd04c3b206d9505",
            ClipHash.compute(listOf(ClipRep.Rtf("e1xydGYxfQ=="))),
        )
    }

    @Test
    fun `hashes a mixed ordered representation list`() {
        assertEquals(
            "cf0057cec3abe111b602c29926e4422b021a35a62bfce928c73f0092b61a72e3",
            ClipHash.compute(MIXED),
        )
    }

    @Test
    fun `hashes an empty representation list to the digest of no bytes`() {
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            ClipHash.compute(emptyList()),
        )
    }

    @Test
    fun `lays out the canonical form as kind unit body record`() {
        val unit = byteArrayOf(ProtocolConstants.CANONICAL_UNIT_SEPARATOR)
        val record = byteArrayOf(ProtocolConstants.CANONICAL_RECORD_SEPARATOR)
        assertArrayEquals(
            "text".toByteArray() + unit + "hi".toByteArray() + record,
            ClipHash.canonicalBytes(listOf(ClipRep.Text("hi"))),
        )
    }

    @Test
    fun `ignores fields that the canonical form excludes`() {
        val renamedImage = ClipRep.Image("image/jpeg", "other.jpg", 1L, "aabbcc", "9c8b7a6d-0000-0000-0000-000000000000")
        assertEquals(
            ClipHash.compute(listOf(ClipRep.Image("image/png", "shot.png", 91_234L, "aabbcc", TRANSFER_ID))),
            ClipHash.compute(listOf(renamedImage)),
        )
    }

    @Test
    fun `distinguishes representation order`() {
        assertNotEquals(ClipHash.compute(MIXED), ClipHash.compute(MIXED.reversed()))
    }

    @Test
    fun `cannot be forged by content containing a separator`() {
        val forged = listOf(ClipRep.Text("hellourlhttps://example.com"))
        assertNotEquals(
            ClipHash.compute(listOf(ClipRep.Text("hello"), ClipRep.Url("https://example.com"))),
            ClipHash.compute(forged),
        )
    }

    private companion object {
        const val TRANSFER_ID = "3f2a1b0c-4d5e-6f70-8192-a3b4c5d6e7f8"

        val MIXED = listOf(
            ClipRep.Text("hello"),
            ClipRep.Url("https://example.com", title = "Example"),
            ClipRep.File("text/plain", "notes.txt", 12L, "0f1e2d", TRANSFER_ID, rel = "docs/notes.txt"),
        )
    }
}
