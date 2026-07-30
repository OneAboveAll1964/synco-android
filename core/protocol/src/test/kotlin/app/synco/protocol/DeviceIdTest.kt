package app.synco.protocol

import app.synco.protocol.encoding.Hex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceIdTest {

    @Test
    fun `derives sixteen characters from the first ten bytes of the identity hash`() {
        val hash = Hex.decode("a1b2c3d4e5f60718293a4b5c6d7e8f90a1b2c3d4e5f60718293a4b5c6d7e8f90")
        val deviceId = DeviceId.fromIdentityHash(hash)
        assertEquals(ProtocolConstants.DEVICE_ID_LENGTH, deviceId.value.length)
        assertEquals(deviceId, DeviceId.fromIdentityHash(hash.copyOf(ProtocolConstants.DEVICE_ID_HASH_BYTES)))
    }

    @Test
    fun `rejects an id of the wrong length or alphabet`() {
        assertThrows(IllegalArgumentException::class.java) { DeviceId("short") }
        assertThrows(IllegalArgumentException::class.java) { DeviceId("ABCDEFGHIJ234567") }
        assertThrows(IllegalArgumentException::class.java) { DeviceId("abcdefghij2345670") }
        assertThrows(IllegalArgumentException::class.java) { DeviceId("abcdefghij23456!") }
        assertNull(DeviceId.parseOrNull("nope"))
    }

    @Test
    fun `orders by the raw ascii id so both sides agree on the initiator`() {
        val lower = DeviceId("abcdefghij234567")
        val upper = DeviceId("zyxwvutsrq765432")
        assertTrue(lower < upper)
        assertEquals(lower.value, minOf(lower, upper).value)
    }

    @Test
    fun `renders as the bare id`() {
        assertEquals("abcdefghij234567", DeviceId("abcdefghij234567").toString())
    }
}
