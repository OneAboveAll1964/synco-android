package app.synco.transfer

import android.content.ContentResolver
import app.synco.logging.SyncoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BlobStager(
    private val resolver: ContentResolver,
    private val storage: TransferStorage,
) {
    suspend fun stage(transferId: UUID, source: TransferSource, maxBytes: Long): StagedBlob? =
        withContext(Dispatchers.IO) {
            val destination = storage.stagingFile(transferId)
            runCatching { copy(source, destination, maxBytes) }
                .onFailure {
                    SyncoLog.transfer.warn("could not stage ${source.mime} for sending", it)
                    destination.delete()
                }
                .getOrNull()
        }

    private fun copy(source: TransferSource, destination: File, maxBytes: Long): StagedBlob? {
        destination.parentFile?.mkdirs()
        val digest = Sha256.newDigest()
        var size = 0L
        source.openStream(resolver).use { input ->
            destination.outputStream().use { output ->
                size = transfer(input, output, digest, maxBytes) ?: return abort(destination, maxBytes)
            }
        }
        return StagedBlob(destination, Sha256.hexOf(digest), size)
    }

    private fun transfer(
        input: InputStream,
        output: OutputStream,
        digest: java.security.MessageDigest,
        maxBytes: Long,
    ): Long? {
        val buffer = ByteArray(BUFFER_BYTES)
        var size = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) return size
            size += read
            if (size > maxBytes) return null
            digest.update(buffer, 0, read)
            output.write(buffer, 0, read)
        }
    }

    private fun abort(destination: File, maxBytes: Long): StagedBlob? {
        SyncoLog.transfer.warn("stopped staging a blob that exceeded the cap of $maxBytes bytes")
        destination.delete()
        return null
    }

    private companion object {
        const val BUFFER_BYTES = 65_536
    }
}
