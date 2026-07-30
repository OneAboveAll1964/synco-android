package app.synco.protocol.message

import app.synco.protocol.DeviceId
import app.synco.protocol.Platform
import app.synco.protocol.ProtocolConstants
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.HELLO)
data class Hello(
    @SerialName("did") val deviceId: DeviceId,
    @SerialName("dn") val displayName: String,
    @SerialName("pl") val platform: Platform,
    @SerialName("ePub") val ephemeralPublicKey: String,
    @SerialName("v") val version: Int = ProtocolConstants.VERSION,
) : ControlMessage
