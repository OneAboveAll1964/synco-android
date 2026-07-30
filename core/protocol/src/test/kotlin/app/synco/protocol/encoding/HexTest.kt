package app.synco.protocol.encoding

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class HexTest {

    @Test
    fun `encodes lowercase and uppercase`() {
        val bytes = byteArrayOf(0x00, 0x0F, 0x7F, -1, -128, 0x1A)
        assertEquals("000f7fff801a", Hex.encodeLower(bytes))
        assertEquals("000F7FFF801A", Hex.encodeUpper(bytes))
    }

    @Test
    fun `decodes either case`() {
        val expected = byteArrayOf(-95, -78, -61, -44)
        assertArrayEquals(expected, Hex.decode("a1b2c3d4"))
        assertArrayEquals(expected, Hex.decode("A1B2C3D4"))
    }

    @Test
    fun `round trips arbitrary bytes`() {
        val bytes = ByteArray(64) { (it * 5 - 11).toByte() }
        assertArrayEquals(bytes, Hex.decode(Hex.encodeLower(bytes)))
        assertArrayEquals(bytes, Hex.decode(Hex.encodeUpper(bytes)))
    }

    @Test
    fun `rejects odd lengths and invalid digits`() {
        assertThrows(IllegalArgumentException::class.java) { Hex.decode("abc") }
        assertThrows(IllegalArgumentException::class.java) { Hex.decode("zz") }
    }
}
