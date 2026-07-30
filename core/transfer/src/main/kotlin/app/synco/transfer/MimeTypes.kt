package app.synco.transfer

import android.webkit.MimeTypeMap

object MimeTypes {

    fun forName(name: String): String {
        val extension = name.substringAfterLast('.', "").lowercase()
        if (extension.isEmpty()) return ContentMetadata.DEFAULT_MIME
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: ContentMetadata.DEFAULT_MIME
    }
}
