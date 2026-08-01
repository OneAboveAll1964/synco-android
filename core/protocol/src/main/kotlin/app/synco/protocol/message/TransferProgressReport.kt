package app.synco.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.TRANSFER_PROGRESS)
data class TransferProgressReport(
    @SerialName("transferId") val transferId: String,
    @SerialName("received") val receivedBytes: Long,
) : ControlMessage
