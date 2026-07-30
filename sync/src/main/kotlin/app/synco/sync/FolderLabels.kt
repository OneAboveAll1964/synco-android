package app.synco.sync

import android.net.Uri

object FolderLabels {

    fun of(treeUri: Uri): String {
        val documentId = treeUri.lastPathSegment ?: return FALLBACK
        val path = documentId.substringAfter(':', documentId).trim('/')
        return path.ifBlank { FALLBACK }
    }

    private const val FALLBACK = "chosen folder"
}
