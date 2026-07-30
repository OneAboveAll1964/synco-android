package app.synco.transfer

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File

class ContentUriMetadata(private val resolver: ContentResolver) {

    fun resolve(uri: Uri): ContentMetadata {
        var queriedName: String? = null
        var queriedSize: Long? = null
        runCatching {
            resolver.query(uri, PROJECTION, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    queriedName = cursor.stringOrNull(OpenableColumns.DISPLAY_NAME)
                    queriedSize = cursor.longOrNull(OpenableColumns.SIZE)
                }
            }
        }
        val mime = mimeOf(uri)
        return ContentMetadata(
            name = queriedName?.takeIf { it.isNotBlank() }?.let { SafeFileName.of(it) } ?: fallbackName(uri, mime),
            mime = mime,
            size = queriedSize?.takeIf { it >= 0 } ?: measure(uri),
        )
    }

    fun isStreamable(uri: Uri): Boolean =
        uri.scheme == ContentResolver.SCHEME_CONTENT || uri.scheme == ContentResolver.SCHEME_FILE

    private fun mimeOf(uri: Uri): String {
        resolver.getType(uri)?.takeIf { it.isNotBlank() }?.let { return it }
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString()).lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: ContentMetadata.DEFAULT_MIME
    }

    private fun fallbackName(uri: Uri, mime: String): String {
        val segment = uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
        val base = SafeFileName.of(segment ?: SafeFileName.FALLBACK)
        if (base.contains('.')) return base
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
        return if (extension.isNullOrBlank()) base else "$base.$extension"
    }

    private fun measure(uri: Uri): Long = when (uri.scheme) {
        ContentResolver.SCHEME_FILE -> uri.path?.let { File(it).length() } ?: ContentMetadata.UNKNOWN_SIZE
        else -> runCatching {
            resolver.openAssetFileDescriptor(uri, READ_MODE)?.use { it.length }
        }.getOrNull()?.takeIf { it >= 0 } ?: ContentMetadata.UNKNOWN_SIZE
    }

    private companion object {
        const val READ_MODE = "r"
        val PROJECTION = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
    }
}
