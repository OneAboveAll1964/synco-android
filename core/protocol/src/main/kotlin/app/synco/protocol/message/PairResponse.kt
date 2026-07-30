package app.synco.protocol.message

import app.synco.protocol.DeviceId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.PAIR_RESPONSE)
data class PairResponse(
    @SerialName("accepted") val accepted: Boolean,
    @SerialName("did") val deviceId: DeviceId,
    @SerialName("sPub") val staticPublicKey: String,
) : ControlMessage
