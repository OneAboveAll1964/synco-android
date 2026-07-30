package app.synco.crypto

import app.synco.protocol.DeviceId

enum class HandshakeRole {
    INITIATOR,
    RESPONDER,
    ;

    val dials: Boolean get() = this == INITIATOR

    companion object {
        fun of(selfDeviceId: DeviceId, peerDeviceId: DeviceId): HandshakeRole =
            if (selfDeviceId < peerDeviceId) INITIATOR else RESPONDER
    }
}
