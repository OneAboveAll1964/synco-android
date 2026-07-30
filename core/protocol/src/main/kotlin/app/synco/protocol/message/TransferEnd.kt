package app.synco.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.TRANSFER_END)
data class TransferEnd(
    @SerialName("transferId") val transferId: String,
    @SerialName("ok") val ok: Boolean,
) : ControlMessage
