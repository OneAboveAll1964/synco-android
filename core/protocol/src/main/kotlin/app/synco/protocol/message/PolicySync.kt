package app.synco.protocol.message

import app.synco.protocol.ProtocolConstants
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.POLICY)
data class PolicySync(
    @SerialName("rev") val revision: Long,
    @SerialName("send") val send: CapsFlags,
    @SerialName("recv") val receive: CapsFlags,
    @SerialName("paused") val paused: Boolean = false,
    @SerialName("maxBlob") val maxBlobBytes: Long = ProtocolConstants.DEFAULT_MAX_BLOB_BYTES,
) : ControlMessage {

    fun mirrored(): PolicySync = copy(send = receive, receive = send)
}
