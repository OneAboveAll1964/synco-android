package app.synco.discovery

import app.synco.protocol.DeviceId
import app.synco.protocol.Fingerprint
import app.synco.protocol.Platform
import java.time.Instant

data class DiscoveredPeer(
    val deviceId: DeviceId,
    val displayName: String,
    val platform: Platform,
    val fingerprint: Fingerprint,
    val host: String,
    val port: Int,
    val lastSeen: Instant,
) {
    val identity: AdvertisedIdentity
        get() = AdvertisedIdentity(deviceId, displayName, platform, fingerprint)

    companion object {
        fun of(identity: AdvertisedIdentity, host: String, port: Int, lastSeen: Instant): DiscoveredPeer =
            DiscoveredPeer(
                deviceId = identity.deviceId,
                displayName = identity.displayName,
                platform = identity.platform,
                fingerprint = identity.fingerprint,
                host = host,
                port = port,
                lastSeen = lastSeen,
            )
    }
}
