package app.synco.discovery

import app.synco.protocol.DeviceId

sealed interface PeerEvent {
    val deviceId: DeviceId

    data class Appeared(val peer: DiscoveredPeer) : PeerEvent {
        override val deviceId: DeviceId get() = peer.deviceId
    }

    data class Disappeared(override val deviceId: DeviceId) : PeerEvent
}
