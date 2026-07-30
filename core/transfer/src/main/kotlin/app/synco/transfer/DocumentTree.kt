package app.synco.transfer

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import app.synco.logging.SyncoLog

class DocumentTree(private val resolver: ContentResolver) {

    fun rootOf(treeUri: Uri): Uri? = runCatching {
        DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))
    }.onFailure { SyncoLog.transfer.warn("the chosen folder is no longer usable", it) }.getOrNull()

    fun directory(treeUri: Uri, parent: Uri, name: String): Uri? =
        findChild(treeUri, parent, name, directoriesOnly = true)
            ?: create(parent, DocumentsContract.Document.MIME_TYPE_DIR, name)

    fun createFile(parent: Uri, mime: String, name: String): Uri? = create(parent, mime, name)

    fun displayName(uri: Uri): String? = query(uri, DocumentsContract.Document.COLUMN_DISPLAY_NAME)

    private fun create(parent: Uri, mime: String, name: String): Uri? = runCatching {
        DocumentsContract.createDocument(resolver, parent, mime, name)
    }.onFailure { SyncoLog.transfer.warn("could not create $name in the chosen folder", it) }.getOrNull()

    private fun findChild(treeUri: Uri, parent: Uri, name: String, directoriesOnly: Boolean): Uri? {
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri,
            DocumentsContract.getDocumentId(parent),
        )
        val columns = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
        )
        return runCatching {
            resolver.query(children, columns, null, null, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val isDirectory = cursor.getString(2) == DocumentsContract.Document.MIME_TYPE_DIR
                    if (cursor.getString(1) == name && (!directoriesOnly || isDirectory)) {
                        return@use DocumentsContract.buildDocumentUriUsingTree(treeUri, cursor.getString(0))
                    }
                }
                null
            }
        }.getOrNull()
    }

    private fun query(uri: Uri, column: String): String? = runCatching {
        resolver.query(uri, arrayOf(column), null, null, null)?.use {
            if (it.moveToFirst()) it.getString(0) else null
        }
    }.getOrNull()
}
