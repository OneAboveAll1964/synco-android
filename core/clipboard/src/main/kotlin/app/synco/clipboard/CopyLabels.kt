package app.synco.clipboard

class CopyLabels(labels: Collection<String>) {

    private val normalized: Set<String> = labels
        .map { it.trim().lowercase() }
        .filter { it.isNotEmpty() }
        .toSet()

    fun matches(candidate: String?): Boolean {
        val value = candidate?.trim()?.lowercase() ?: return false
        if (value in normalized) return true
        return normalized.any { label -> value.startsWith("$label ") }
    }

    companion object {
        val KNOWN: List<String> = listOf(
            "copy",
            "copy text",
            "copy link",
            "copy link address",
            "copy image",
        )

        fun withFallback(resolved: String?): CopyLabels {
            val primary = resolved?.trim().orEmpty()
            val labels = if (primary.isEmpty()) KNOWN else KNOWN + primary
            return CopyLabels(labels)
        }
    }
}
