package app.synco.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.SHIZUKU_START)
data object ShizukuStartRequest : ControlMessage
