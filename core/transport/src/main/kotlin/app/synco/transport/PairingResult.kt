package app.synco.transport

import app.synco.protocol.DeviceId

sealed interface PairingResult {
    val peerDeviceId: DeviceId

    data class Approved(val peer: PeerDescriptor) : PairingResult {
        override val peerDeviceId: DeviceId get() = peer.deviceId
    }

    data class DeclinedLocally(override val peerDeviceId: DeviceId) : PairingResult

    data class DeclinedByPeer(override val peerDeviceId: DeviceId) : PairingResult
}
