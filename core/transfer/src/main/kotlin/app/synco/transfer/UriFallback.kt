package app.synco.transfer

import android.net.Uri
import java.io.InputStream

interface UriOpener {
    fun open(uri: Uri): InputStream?

    fun describe(uri: Uri): UriFacts?
}

data class UriFacts(val name: String?, val mime: String?, val size: Long?)

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

    fun describe(uri: Uri): UriFacts? = opener?.let { runCatching { it.describe(uri) }.getOrNull() }
}
