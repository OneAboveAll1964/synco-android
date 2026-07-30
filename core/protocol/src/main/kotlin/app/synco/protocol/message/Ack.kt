package app.synco.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.ACK)
data class Ack(
    @SerialName("id") val clipId: String,
    @SerialName("applied") val applied: Boolean,
    @SerialName("reason") val reason: String? = null,
) : ControlMessage {
    val ackReason: AckReason? get() = AckReason.fromWire(reason)

    companion object {
        fun applied(clipId: String): Ack = Ack(clipId, applied = true)

        fun rejected(clipId: String, reason: AckReason): Ack =
            Ack(clipId, applied = false, reason = reason.wireValue)
    }
}
