package app.synco.transfer

import android.net.Uri
import java.io.InputStream

fun interface UriOpener {
    fun open(uri: Uri): InputStream?
}

object UriFallback {

    @Volatile
    private var opener: UriOpener? = null

    fun install(opener: UriOpener) {
        this.opener = opener
    }

    fun clear() {
        opener = null
    }

    fun open(uri: Uri): InputStream? = opener?.let { runCatching { it.open(uri) }.getOrNull() }
}
