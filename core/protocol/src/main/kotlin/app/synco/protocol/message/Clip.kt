package app.synco.protocol.message

import app.synco.protocol.DeviceId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.CLIP)
data class Clip(
    @SerialName("id") val id: String,
    @SerialName("ts") val timestampMillis: Long,
    @SerialName("origin") val origin: DeviceId,
    @SerialName("hash") val hash: String,
    @SerialName("reps") val reps: List<ClipRep>,
) : ControlMessage {
    val streamedReps: List<ClipRep> get() = reps.filter { it.kind in ClipRepKind.STREAMED }
}
