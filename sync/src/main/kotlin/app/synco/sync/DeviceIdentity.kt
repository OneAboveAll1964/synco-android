package app.synco.sync

import app.synco.protocol.DeviceId
import app.synco.protocol.Fingerprint
import app.synco.protocol.Platform
import app.synco.transport.LocalDevice

data class DeviceIdentity(
    val deviceId: DeviceId,
    val displayName: String,
    val platform: Platform,
    val fingerprint: Fingerprint,
) {
    companion object {
        fun of(local: LocalDevice): DeviceIdentity = DeviceIdentity(
            deviceId = local.deviceId,
            displayName = local.displayName,
            platform = local.platform,
            fingerprint = local.fingerprint,
        )
    }
}
