package app.synco.protocol

import app.synco.protocol.encoding.Hex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class FingerprintTest {

    @Test
    fun `groups the first eight bytes of the identity hash into four char blocks`() {
        val hash = Hex.decode("a1b2c3d4e5f60718293a4b5c6d7e8f90")
        assertEquals("A1B2-C3D4-E5F6-0718", Fingerprint.fromIdentityHash(hash).grouped)
    }

    @Test
    fun `exposes the unseparated form for txt records`() {
        assertEquals("A1B2C3D4E5F60718", Fingerprint("A1B2-C3D4-E5F6-0718").plain)
        assertEquals(ProtocolConstants.FINGERPRINT_HEX_LENGTH, Fingerprint("A1B2-C3D4-E5F6-0718").plain.length)
    }

    @Test
    fun `parses both the grouped and the unseparated form`() {
        val expected = Fingerprint("A1B2-C3D4-E5F6-0718")
        assertEquals(expected, Fingerprint.parseOrNull("A1B2-C3D4-E5F6-0718"))
        assertEquals(expected, Fingerprint.parseOrNull("A1B2C3D4E5F60718"))
        assertEquals(expected, Fingerprint.parseOrNull("a1b2c3d4e5f60718"))
        assertEquals(expected, Fingerprint.fromPlain("a1b2c3d4e5f60718"))
    }

    @Test
    fun `rejects anything that is not four uppercase hex blocks`() {
        assertThrows(IllegalArgumentException::class.java) { Fingerprint("A1B2C3D4E5F60718") }
        assertThrows(IllegalArgumentException::class.java) { Fingerprint("a1b2-c3d4-e5f6-0718") }
        assertThrows(IllegalArgumentException::class.java) { Fingerprint("A1B2-C3D4-E5F6") }
        assertNull(Fingerprint.parseOrNull("A1B2-C3D4"))
    }
}
