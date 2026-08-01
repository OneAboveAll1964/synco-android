package app.synco.transfer

import android.content.ContentResolver
import android.net.Uri
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

sealed interface TransferSource {
    val name: String
    val mime: String
    val declaredSize: Long

    fun openStream(resolver: ContentResolver): InputStream

    class Content(
        val uri: Uri,
        override val name: String,
        override val mime: String,
        override val declaredSize: Long,
    ) : TransferSource {
        override fun openStream(resolver: ContentResolver): InputStream =
            runCatching { resolver.openInputStream(uri) }.getOrNull()
                ?: UriFallback.open(uri)
                ?: throw FileNotFoundException("cannot open $uri")
    }

    class Local(
        val file: File,
        override val mime: String,
        override val name: String = file.name,
    ) : TransferSource {
        override val declaredSize: Long get() = file.length()

        override fun openStream(resolver: ContentResolver): InputStream = file.inputStream()
    }

    companion object {
        fun content(uri: Uri, metadata: ContentMetadata): Content =
            Content(uri, metadata.name, metadata.mime, metadata.size)
    }
}
