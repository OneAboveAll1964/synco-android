package app.synco.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.TRANSFER_START)
data class TransferStart(
    @SerialName("transferId") val transferId: String,
    @SerialName("clipId") val clipId: String,
    @SerialName("name") val name: String,
    @SerialName("mime") val mime: String,
    @SerialName("size") val size: Long,
    @SerialName("sha256") val sha256: String,
) : ControlMessage
