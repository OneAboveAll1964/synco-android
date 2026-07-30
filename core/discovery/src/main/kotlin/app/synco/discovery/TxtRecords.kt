package app.synco.discovery

import app.synco.protocol.DeviceId
import app.synco.protocol.DiscoveryConstants
import app.synco.protocol.Fingerprint
import app.synco.protocol.Platform
import app.synco.protocol.ProtocolConstants

object TxtRecords {

    private const val CONTINUATION_MASK = 0xC0
    private const val CONTINUATION_MARKER = 0x80

    fun build(identity: AdvertisedIdentity): Map<String, String> = mapOf(
        DiscoveryConstants.TXT_KEY_VERSION to ProtocolConstants.VERSION.toString(),
        DiscoveryConstants.TXT_KEY_DEVICE_ID to identity.deviceId.value,
        DiscoveryConstants.TXT_KEY_DISPLAY_NAME to truncateDisplayName(identity.displayName),
        DiscoveryConstants.TXT_KEY_PLATFORM to identity.platform.wireValue,
        DiscoveryConstants.TXT_KEY_FINGERPRINT to identity.fingerprint.plain,
    )

    fun parse(attributes: Map<String, ByteArray?>): AdvertisedIdentity? {
        val values = attributes.mapValues { (_, value) -> value?.decodeToString() }
        val version = values[DiscoveryConstants.TXT_KEY_VERSION]?.toIntOrNull()
        if (version != ProtocolConstants.VERSION) return null
        val deviceId = values[DiscoveryConstants.TXT_KEY_DEVICE_ID]
            ?.let { DeviceId.parseOrNull(it) } ?: return null
        val platform = values[DiscoveryConstants.TXT_KEY_PLATFORM]
            ?.let { Platform.fromWire(it) } ?: return null
        val fingerprint = values[DiscoveryConstants.TXT_KEY_FINGERPRINT]
            ?.let { Fingerprint.parseOrNull(it) } ?: return null
        val displayName = values[DiscoveryConstants.TXT_KEY_DISPLAY_NAME]
            ?.takeIf { it.isNotBlank() }
            ?: deviceId.value
        return AdvertisedIdentity(deviceId, displayName, platform, fingerprint)
    }

    fun truncateDisplayName(displayName: String): String {
        val encoded = displayName.encodeToByteArray()
        if (encoded.size <= DiscoveryConstants.DISPLAY_NAME_MAX_BYTES) return displayName
        var end = DiscoveryConstants.DISPLAY_NAME_MAX_BYTES
        while (end > 0 && isContinuation(encoded[end])) end--
        return encoded.decodeToString(0, end)
    }

    private fun isContinuation(byte: Byte): Boolean =
        (byte.toInt() and CONTINUATION_MASK) == CONTINUATION_MARKER
}
