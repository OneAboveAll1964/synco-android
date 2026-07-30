package app.synco.storage

import app.synco.protocol.message.CapsFlags
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PeerDirections(
    @SerialName("send") val send: CapsFlags = CapsFlags.ALL_ENABLED,
    @SerialName("recv") val receive: CapsFlags = CapsFlags.ALL_ENABLED,
) {
    companion object {
        val ALL_ENABLED = PeerDirections()
        val NONE = PeerDirections(send = CapsFlags.ALL_DISABLED, receive = CapsFlags.ALL_DISABLED)
    }
}
