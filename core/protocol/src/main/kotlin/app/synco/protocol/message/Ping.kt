package app.synco.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.PING)
data class Ping(
    @SerialName("seq") val sequence: Long,
) : ControlMessage
