package app.synco.transfer

import android.content.ContentResolver
import android.net.Uri
import app.synco.logging.SyncoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DocumentTreeDestination(
    private val resolver: ContentResolver,
    private val treeUri: () -> Uri?,
    private val folderLabel: () -> String,
) : ReceivedFileDestination {

    private val tree = DocumentTree(resolver)

    override val isChosen: Boolean get() = treeUri() != null

    override suspend fun publish(source: File, name: String, relativePath: String?): PublishedFile? =
        withContext(Dispatchers.IO) {
            val root = treeUri() ?: return@withContext null
            val parent = parentFor(root, relativePath) ?: return@withContext null
            val mime = MimeTypes.forName(name)
            val document = tree.createFile(parent, mime, name) ?: return@withContext null
            if (!copy(source, document)) {
                SyncoLog.transfer.warn("could not write $name into the chosen folder")
                return@withContext null
            }
            val finalName = tree.displayName(document) ?: name
            PublishedFile(finalName, "${folderLabel()}/$finalName", document)
        }

    private fun parentFor(root: Uri, relativePath: String?): Uri? {
        val start = tree.rootOf(root) ?: return null
        val segments = relativePath?.split('/')?.filter { it.isNotBlank() }.orEmpty()
        return segments.fold(start) { parent, segment ->
            tree.directory(root, parent, SafeFileName.of(segment)) ?: return null
        }
    }

    private fun copy(source: File, document: Uri): Boolean = runCatching {
        resolver.openOutputStream(document)?.use { output ->
            source.inputStream().use { input -> input.copyTo(output) }
        } != null
    }.onFailure { SyncoLog.transfer.warn("could not copy into the chosen folder", it) }.getOrDefault(false)
}
