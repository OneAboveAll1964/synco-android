package app.synco.transfer

data class ContentMetadata(
    val name: String,
    val mime: String,
    val size: Long,
) {
    val isImage: Boolean get() = mime.startsWith(IMAGE_MIME_PREFIX)

    val hasKnownSize: Boolean get() = size >= 0

    companion object {
        const val IMAGE_MIME_PREFIX = "image/"
        const val DEFAULT_MIME = "application/octet-stream"
        const val UNKNOWN_SIZE = -1L
    }
}
