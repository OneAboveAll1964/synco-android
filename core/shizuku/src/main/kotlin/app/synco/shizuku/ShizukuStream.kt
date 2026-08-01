package app.synco.shizuku

import android.net.Uri
import app.synco.logging.SyncoLog
import rikka.shizuku.Shizuku
import java.io.InputStream

object ShizukuStream {

    fun open(uri: Uri): InputStream? {
        if (!runCatching { Shizuku.pingBinder() }.getOrDefault(false)) return null
        HiddenApi.open()
        return runCatching { read(uri) }
            .onFailure { SyncoLog.clipboard.warn("Shizuku could not open $uri", it) }
            .getOrNull()
    }

    private fun read(uri: Uri): InputStream? {
        val method = Shizuku::class.java.declaredMethods
            .firstOrNull { it.name == NEW_PROCESS && it.parameterTypes.size == 3 }
            ?: return null
        method.isAccessible = true
        val process = method.invoke(
            null,
            arrayOf("content", "read", "--uri", uri.toString()),
            null,
            null,
        ) as? Process ?: return null
        return process.inputStream
    }

    private const val NEW_PROCESS = "newProcess"
}
