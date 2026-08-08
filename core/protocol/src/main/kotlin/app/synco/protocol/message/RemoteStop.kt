package app.synco.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.REMOTE_STOP)
data object RemoteStop : ControlMessage
