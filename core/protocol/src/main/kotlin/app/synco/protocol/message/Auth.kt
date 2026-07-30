package app.synco.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.AUTH)
data class Auth(
    @SerialName("tag") val tag: String,
) : ControlMessage
