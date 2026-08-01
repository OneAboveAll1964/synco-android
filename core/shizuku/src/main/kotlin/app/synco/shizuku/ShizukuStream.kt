package app.synco.shizuku

import android.net.Uri
import app.synco.logging.SyncoLog
import app.synco.transfer.UriFacts
import app.synco.transfer.UriOpener
import rikka.shizuku.Shizuku
import java.io.InputStream

object ShizukuStream : UriOpener {

    override fun open(uri: Uri): InputStream? = spawn(
        arrayOf("content", "read", "--uri", uri.toString()),
    )?.inputStream

    override fun describe(uri: Uri): UriFacts? {
        val process = spawn(
            arrayOf(
                "content",
                "query",
                "--uri",
                uri.toString(),
                "--projection",
                ShizukuUriFacts.COLUMNS.joinToString(":"),
            ),
        ) ?: return null
        val output = runCatching { process.inputStream.bufferedReader().readText() }.getOrNull()
        runCatching { process.waitFor() }
        return output?.let(ShizukuUriFacts::parse)
    }

    private fun spawn(command: Array<String>): Process? {
        if (!runCatching { Shizuku.pingBinder() }.getOrDefault(false)) return null
        HiddenApi.open()
        return runCatching { newProcess(command) }
            .onFailure { SyncoLog.clipboard.warn("Shizuku could not run ${command.firstOrNull()}", it) }
            .getOrNull()
    }

    private fun newProcess(command: Array<String>): Process? {
        val method = Shizuku::class.java.declaredMethods
            .firstOrNull { it.name == NEW_PROCESS && it.parameterTypes.size == 3 }
            ?: return null
        method.isAccessible = true
        return method.invoke(null, command, null, null) as? Process
    }

    private const val NEW_PROCESS = "newProcess"
}
