package app.synco.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.REMOTE_REJECT)
data class RemoteReject(
    @SerialName("reason") val reason: String,
) : ControlMessage
