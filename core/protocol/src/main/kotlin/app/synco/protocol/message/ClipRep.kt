package app.synco.protocol.message

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator(ClipRepKind.DISCRIMINATOR)
sealed interface ClipRep {
    val kind: String

    @Serializable
    @SerialName(ClipRepKind.TEXT)
    data class Text(
        @SerialName("text") val text: String,
    ) : ClipRep {
        override val kind: String get() = ClipRepKind.TEXT
    }

    @Serializable
    @SerialName(ClipRepKind.HTML)
    data class Html(
        @SerialName("html") val html: String,
    ) : ClipRep {
        override val kind: String get() = ClipRepKind.HTML
    }

    @Serializable
    @SerialName(ClipRepKind.RTF)
    data class Rtf(
        @SerialName("b64") val base64: String,
    ) : ClipRep {
        override val kind: String get() = ClipRepKind.RTF
    }

    @Serializable
    @SerialName(ClipRepKind.URL)
    data class Url(
        @SerialName("url") val url: String,
        @SerialName("title") val title: String? = null,
    ) : ClipRep {
        override val kind: String get() = ClipRepKind.URL
    }

    @Serializable
    @SerialName(ClipRepKind.IMAGE)
    data class Image(
        @SerialName("mime") val mime: String,
        @SerialName("name") val name: String,
        @SerialName("size") val size: Long,
        @SerialName("sha256") val sha256: String,
        @SerialName("transferId") val transferId: String,
    ) : ClipRep {
        override val kind: String get() = ClipRepKind.IMAGE
    }

    @Serializable
    @SerialName(ClipRepKind.FILE)
    data class File(
        @SerialName("mime") val mime: String,
        @SerialName("name") val name: String,
        @SerialName("size") val size: Long,
        @SerialName("sha256") val sha256: String,
        @SerialName("transferId") val transferId: String,
        @SerialName("rel") val rel: String? = null,
    ) : ClipRep {
        override val kind: String get() = ClipRepKind.FILE
    }
}
