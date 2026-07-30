package app.synco.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.BYE)
data class Bye(
    @SerialName("reason") val reason: String? = null,
) : ControlMessage {
    val closeReason: CloseReason? get() = CloseReason.fromWire(reason)

    companion object {
        fun of(reason: CloseReason): Bye = Bye(reason.wireValue)
    }
}
