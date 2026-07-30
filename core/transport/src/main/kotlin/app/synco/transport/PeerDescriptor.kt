package app.synco.transport

import app.synco.crypto.PeerIdentity
import app.synco.protocol.DeviceId
import app.synco.protocol.Fingerprint
import app.synco.protocol.Platform

class PeerDescriptor(
    val deviceId: DeviceId,
    val displayName: String,
    val platform: Platform,
    staticPublicKey: ByteArray,
) {
    private val key: ByteArray = staticPublicKey.copyOf()

    val staticPublicKey: ByteArray get() = key.copyOf()

    val fingerprint: Fingerprint = PeerIdentity.fingerprintOf(key)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PeerDescriptor) return false
        return deviceId == other.deviceId &&
            displayName == other.displayName &&
            platform == other.platform &&
            key.contentEquals(other.key)
    }

    override fun hashCode(): Int {
        var result = deviceId.hashCode()
        result = result * HASH_MULTIPLIER + displayName.hashCode()
        result = result * HASH_MULTIPLIER + platform.hashCode()
        result = result * HASH_MULTIPLIER + key.contentHashCode()
        return result
    }

    override fun toString(): String =
        "PeerDescriptor($deviceId, $displayName, ${platform.wireValue}, $fingerprint)"

    private companion object {
        const val HASH_MULTIPLIER = 31
    }
}
