package app.synco.protocol.message

object ClipRepKind {
    const val DISCRIMINATOR = "k"

    const val TEXT = "text"
    const val HTML = "html"
    const val RTF = "rtf"
    const val URL = "url"
    const val IMAGE = "image"
    const val FILE = "file"

    val INLINE: Set<String> = setOf(TEXT, HTML, RTF, URL)
    val STREAMED: Set<String> = setOf(IMAGE, FILE)
}
