package app.synco.sync

import app.synco.protocol.message.ClipRep
import app.synco.protocol.message.ClipRepKind

object ClipSummary {

    fun of(reps: List<ClipRep>): String? {
        val images = reps.count { it.kind == ClipRepKind.IMAGE }
        val files = reps.count { it.kind == ClipRepKind.FILE }
        val parts = buildList {
            if (images > 0) add(count(images, "image", "images"))
            if (files > 0) add(count(files, "file", "files"))
        }
        if (parts.isNotEmpty()) return parts.joinToString(" and ")
        if (reps.any { it.kind == ClipRepKind.URL }) return "Link"
        if (reps.any { it.kind == ClipRepKind.TEXT }) return "Text"
        return null
    }

    private fun count(value: Int, one: String, many: String): String =
        if (value == 1) "1 $one" else "$value $many"
}
