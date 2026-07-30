package app.synco.discovery

import app.synco.protocol.DeviceId
import app.synco.protocol.Fingerprint
import app.synco.protocol.Platform

data class AdvertisedIdentity(
    val deviceId: DeviceId,
    val displayName: String,
    val platform: Platform,
    val fingerprint: Fingerprint,
) {
    val serviceName: String get() = deviceId.value
}
