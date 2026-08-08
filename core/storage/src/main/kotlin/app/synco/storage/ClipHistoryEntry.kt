package app.synco.storage

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClipHistoryEntry(
    @SerialName("text") val text: String,
    @SerialName("url") val isUrl: Boolean,
    @SerialName("at") val atMillis: Long,
    @SerialName("peer") val fromPeer: Boolean,
) {
    companion object {
        const val MAX_TEXT_CHARS = 4_096
        const val MAX_ENTRIES = 30
    }
}
