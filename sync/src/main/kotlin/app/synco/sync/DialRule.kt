package app.synco.sync

import app.synco.crypto.HandshakeRole
import app.synco.protocol.DeviceId

internal object DialRule {

    fun roleFor(selfDeviceId: DeviceId, peerDeviceId: DeviceId): HandshakeRole =
        HandshakeRole.of(selfDeviceId, peerDeviceId)

    fun intentOf(
        role: HandshakeRole,
        discovered: Boolean,
        rejected: Boolean,
        live: Boolean,
        trusted: Boolean,
        adoptedManually: Boolean = false,
    ): PeerIntent = when {
        live || rejected || !discovered -> PeerIntent.IDLE
        !trusted -> PeerIntent.WAIT
        role.dials || adoptedManually -> PeerIntent.DIAL
        else -> PeerIntent.WAIT
    }
}
