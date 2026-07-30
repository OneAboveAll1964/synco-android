package app.synco.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CapsFlags(
    @SerialName("text") val text: Boolean,
    @SerialName("image") val image: Boolean,
    @SerialName("file") val file: Boolean,
) {
    val allDisabled: Boolean get() = !text && !image && !file

    companion object {
        val ALL_ENABLED = CapsFlags(text = true, image = true, file = true)
        val ALL_DISABLED = CapsFlags(text = false, image = false, file = false)
    }
}
