package app.synco.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.SHIZUKU_START_RESULT)
data class ShizukuStartResult(
    @SerialName("started") val started: Boolean,
    @SerialName("reason") val reason: String? = null,
) : ControlMessage
