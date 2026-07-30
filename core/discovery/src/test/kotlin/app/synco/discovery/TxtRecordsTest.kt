package app.synco.discovery

import app.synco.protocol.DeviceId
import app.synco.protocol.DiscoveryConstants
import app.synco.protocol.Fingerprint
import app.synco.protocol.Platform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TxtRecordsTest {

    private val identity = AdvertisedIdentity(
        deviceId = DeviceId("abcdefghij234567"),
        displayName = "Pixel 9",
        platform = Platform.ANDROID,
        fingerprint = Fingerprint("A1B2-C3D4-E5F6-0718"),
    )

    @Test
    fun `builds the section 2 keys`() {
        val records = TxtRecords.build(identity)

        assertEquals("1", records[DiscoveryConstants.TXT_KEY_VERSION])
        assertEquals("abcdefghij234567", records[DiscoveryConstants.TXT_KEY_DEVICE_ID])
        assertEquals("Pixel 9", records[DiscoveryConstants.TXT_KEY_DISPLAY_NAME])
        assertEquals("android", records[DiscoveryConstants.TXT_KEY_PLATFORM])
        assertEquals("A1B2C3D4E5F60718", records[DiscoveryConstants.TXT_KEY_FINGERPRINT])
    }

    @Test
    fun `round trips through the wire form`() {
        assertEquals(identity, TxtRecords.parse(encode(TxtRecords.build(identity))))
    }

    @Test
    fun `rejects another protocol version`() {
        val records = TxtRecords.build(identity) + (DiscoveryConstants.TXT_KEY_VERSION to "2")

        assertNull(TxtRecords.parse(encode(records)))
    }

    @Test
    fun `rejects a missing device id`() {
        val records = TxtRecords.build(identity) - DiscoveryConstants.TXT_KEY_DEVICE_ID

        assertNull(TxtRecords.parse(encode(records)))
    }

    @Test
    fun `rejects an unknown platform`() {
        val records = TxtRecords.build(identity) + (DiscoveryConstants.TXT_KEY_PLATFORM to "windows")

        assertNull(TxtRecords.parse(encode(records)))
    }

    @Test
    fun `truncates a long name on a code point boundary`() {
        val truncated = TxtRecords.truncateDisplayName("é".repeat(32))

        assertEquals("é".repeat(31), truncated)
        assertEquals(62, truncated.encodeToByteArray().size)
    }

    @Test
    fun `never splits a four byte code point`() {
        val truncated = TxtRecords.truncateDisplayName("a".repeat(60) + "🙂")

        assertEquals("a".repeat(60), truncated)
    }

    @Test
    fun `keeps a name that already fits`() {
        val fits = "a".repeat(DiscoveryConstants.DISPLAY_NAME_MAX_BYTES)

        assertEquals(fits, TxtRecords.truncateDisplayName(fits))
    }

    private fun encode(records: Map<String, String>): Map<String, ByteArray?> =
        records.mapValues { (_, value) -> value.encodeToByteArray() }
}
