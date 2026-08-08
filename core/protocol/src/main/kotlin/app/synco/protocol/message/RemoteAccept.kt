package app.synco.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.REMOTE_ACCEPT)
data class RemoteAccept(
    @SerialName("codec") val codec: String,
    @SerialName("width") val width: Int,
    @SerialName("height") val height: Int,
    @SerialName("fps") val fps: Int,
    @SerialName("input") val input: Boolean,
) : ControlMessage
