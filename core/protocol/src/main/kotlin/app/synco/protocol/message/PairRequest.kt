package app.synco.protocol.message

import app.synco.protocol.DeviceId
import app.synco.protocol.Fingerprint
import app.synco.protocol.Platform
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.PAIR_REQUEST)
data class PairRequest(
    @SerialName("did") val deviceId: DeviceId,
    @SerialName("dn") val displayName: String,
    @SerialName("pl") val platform: Platform,
    @SerialName("sPub") val staticPublicKey: String,
    @SerialName("fp") val fingerprint: Fingerprint,
) : ControlMessage
