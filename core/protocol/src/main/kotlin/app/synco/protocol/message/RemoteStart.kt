package app.synco.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.REMOTE_START)
data class RemoteStart(
    @SerialName("maxWidth") val maxWidth: Int,
    @SerialName("maxHeight") val maxHeight: Int,
    @SerialName("fps") val fps: Int,
    @SerialName("input") val input: Boolean,
) : ControlMessage
