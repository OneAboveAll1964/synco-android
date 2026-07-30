package app.synco.sync

import app.synco.protocol.DeviceId
import app.synco.protocol.Fingerprint
import app.synco.protocol.Platform
import app.synco.transport.PeerDescriptor

data class PendingPairing(
    val deviceId: DeviceId,
    val displayName: String,
    val platform: Platform,
    val fingerprint: Fingerprint,
    val requestedAtMillis: Long,
) {
    companion object {
        fun of(peer: PeerDescriptor, requestedAtMillis: Long): PendingPairing = PendingPairing(
            deviceId = peer.deviceId,
            displayName = peer.displayName,
            platform = peer.platform,
            fingerprint = peer.fingerprint,
            requestedAtMillis = requestedAtMillis,
        )
    }
}
