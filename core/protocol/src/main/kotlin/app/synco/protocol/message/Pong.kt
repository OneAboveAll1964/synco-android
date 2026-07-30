package app.synco.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.PONG)
data class Pong(
    @SerialName("seq") val sequence: Long,
) : ControlMessage
