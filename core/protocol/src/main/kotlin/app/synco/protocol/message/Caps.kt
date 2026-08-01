package app.synco.protocol.message

import app.synco.protocol.ProtocolConstants
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.CAPS)
data class Caps(
    @SerialName("accepts") val accepts: CapsFlags,
    @SerialName("sends") val sends: CapsFlags,
    @SerialName("maxBlob") val maxBlobBytes: Long = ProtocolConstants.DEFAULT_MAX_BLOB_BYTES,
    @SerialName("adbShizuku") val adbShizuku: Boolean = false,
) : ControlMessage
