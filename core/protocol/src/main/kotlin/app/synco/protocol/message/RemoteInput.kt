package app.synco.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MessageType.REMOTE_INPUT)
data class RemoteInput(
    @SerialName("ev") val events: List<RemoteInputEvent>,
) : ControlMessage

@Serializable
data class RemoteInputEvent(
    @SerialName("k") val kind: String,
    @SerialName("x") val x: Double? = null,
    @SerialName("y") val y: Double? = null,
    @SerialName("dx") val dx: Double? = null,
    @SerialName("dy") val dy: Double? = null,
    @SerialName("btn") val button: String? = null,
    @SerialName("down") val down: Boolean? = null,
    @SerialName("scale") val scale: Double? = null,
    @SerialName("code") val code: Int? = null,
    @SerialName("mods") val mods: Int? = null,
    @SerialName("s") val text: String? = null,
) {
    companion object {
        const val POINTER_ABSOLUTE = "pa"
        const val POINTER_RELATIVE = "pm"
        const val BUTTON = "b"
        const val SCROLL = "s"
        const val MAGNIFY = "z"
        const val KEY = "k"
        const val TEXT = "txt"
    }
}
