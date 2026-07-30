package app.synco.protocol.encoding

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class Base32Test {

    @Test
    fun `encodes the rfc 4648 vectors with the lowercase alphabet and no padding`() {
        VECTORS.forEach { (plain, encoded) ->
            assertEquals(encoded, Base32.encode(plain.toByteArray(Charsets.US_ASCII)))
        }
    }

    @Test
    fun `decodes the rfc 4648 vectors`() {
        VECTORS.forEach { (plain, encoded) ->
            assertArrayEquals(plain.toByteArray(Charsets.US_ASCII), Base32.decode(encoded))
        }
    }

    @Test
    fun `round trips every length up to four blocks`() {
        for (size in 0..40) {
            val bytes = ByteArray(size) { (it * 7 + 3).toByte() }
            assertArrayEquals(bytes, Base32.decode(Base32.encode(bytes)))
        }
    }

    @Test
    fun `encodes a ten byte identity hash to sixteen characters`() {
        val hash = ByteArray(10) { it.toByte() }
        assertEquals(16, Base32.encode(hash).length)
    }

    @Test
    fun `rejects padding characters`() {
        assertThrows(IllegalArgumentException::class.java) { Base32.decode("my======") }
    }

    @Test
    fun `rejects characters outside the alphabet`() {
        assertThrows(IllegalArgumentException::class.java) { Base32.decode("MY") }
        assertThrows(IllegalArgumentException::class.java) { Base32.decode("m0") }
    }

    @Test
    fun `rejects lengths that cannot come from an unpadded encoding`() {
        assertThrows(IllegalArgumentException::class.java) { Base32.decode("m") }
        assertThrows(IllegalArgumentException::class.java) { Base32.decode("mzx") }
    }

    @Test
    fun `rejects non canonical trailing bits`() {
        assertThrows(IllegalArgumentException::class.java) { Base32.decode("mz") }
    }

    private companion object {
        val VECTORS = listOf(
            "" to "",
            "f" to "my",
            "fo" to "mzxq",
            "foo" to "mzxw6",
            "foob" to "mzxw6yq",
            "fooba" to "mzxw6ytb",
            "foobar" to "mzxw6ytboi",
        )
    }
}
