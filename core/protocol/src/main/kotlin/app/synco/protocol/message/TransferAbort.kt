package app.synco.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.TRANSFER_ABORT)
data class TransferAbort(
    @SerialName("transferId") val transferId: String,
    @SerialName("reason") val reason: String? = null,
) : ControlMessage {
    val ackReason: AckReason? get() = AckReason.fromWire(reason)

    companion object {
        fun of(transferId: String, reason: AckReason): TransferAbort =
            TransferAbort(transferId, reason.wireValue)
    }
}
