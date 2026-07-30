package app.synco.transfer

import java.io.File
import java.util.UUID

internal object SafeFileName {
    const val FALLBACK = "clipboard"

    private const val MAX_LENGTH = 120
    private const val MAX_ATTEMPTS = 512
    private const val EXTENSION_SEPARATOR = '.'
    private const val REPLACEMENT = '_'
    private const val FIRST_PRINTABLE = ' '
    private const val ILLEGAL_CHARS = "/:*?<>|"

    fun of(raw: String): String {
        val cleaned = raw.trim()
            .map { if (it < FIRST_PRINTABLE || it in ILLEGAL_CHARS || it == '\\' || it == '"') REPLACEMENT else it }
            .joinToString(separator = "")
            .trim(EXTENSION_SEPARATOR, ' ')
        val limited = if (cleaned.length > MAX_LENGTH) cleaned.takeLast(MAX_LENGTH) else cleaned
        return limited.ifEmpty { FALLBACK }
    }

    fun unique(directory: File, name: String): File {
        for (index in 0..MAX_ATTEMPTS) {
            val candidate = File(directory, if (index == 0) name else numbered(name, index))
            if (runCatching { candidate.createNewFile() }.getOrDefault(false)) return candidate
        }
        return File(directory, "${UUID.randomUUID()}-$name").also { it.createNewFile() }
    }

    fun resolveDirectory(root: File, relativePath: String): File {
        val segments = relativePath.split('/')
            .filter { it.isNotBlank() && it != "." && it != ".." }
            .map { of(it) }
        var resolved = root
        for (segment in segments) resolved = File(resolved, segment)
        return if (isInside(root, resolved)) resolved else root
    }

    private fun numbered(name: String, index: Int): String {
        val separator = name.lastIndexOf(EXTENSION_SEPARATOR)
        if (separator <= 0) return "$name ($index)"
        return "${name.substring(0, separator)} ($index)${name.substring(separator)}"
    }

    private fun isInside(root: File, candidate: File): Boolean {
        val rootPath = runCatching { root.canonicalPath }.getOrNull() ?: return false
        val candidatePath = runCatching { candidate.canonicalPath }.getOrNull() ?: return false
        return candidatePath == rootPath || candidatePath.startsWith(rootPath + File.separator)
    }
}
