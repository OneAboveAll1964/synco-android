package app.synco.shizuku

import app.synco.transfer.UriFacts

object ShizukuUriFacts {

    val COLUMNS = listOf("_display_name", "_size", "mime_type")

    fun parse(output: String): UriFacts? {
        val row = output.lineSequence().firstOrNull { it.startsWith(ROW_PREFIX) } ?: return null
        val fields = fieldsOf(row.removePrefix(ROW_PREFIX).trim().substringAfter(' '))
        val name = fields["_display_name"]?.takeIf { it.isNotBlank() && it != NULL }
        val mime = fields["mime_type"]?.takeIf { it.isNotBlank() && it != NULL }
        val size = fields["_size"]?.takeIf { it != NULL }?.toLongOrNull()
        if (name == null && mime == null && size == null) return null
        return UriFacts(name = name, mime = mime, size = size)
    }

    private fun fieldsOf(row: String): Map<String, String> = row
        .split(", ")
        .mapNotNull { pair ->
            val key = pair.substringBefore('=', "").trim()
            if (key.isEmpty() || !pair.contains('=')) return@mapNotNull null
            key to pair.substringAfter('=').trim()
        }
        .toMap()

    private const val ROW_PREFIX = "Row:"
    private const val NULL = "NULL"
}
