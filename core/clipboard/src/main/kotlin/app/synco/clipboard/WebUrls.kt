package app.synco.clipboard

internal object WebUrls {
    private val SCHEMES = listOf("http://", "https://")

    fun isWebUrl(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || trimmed.any { it.isWhitespace() }) return false
        return SCHEMES.any { trimmed.startsWith(it, ignoreCase = true) }
    }
}
