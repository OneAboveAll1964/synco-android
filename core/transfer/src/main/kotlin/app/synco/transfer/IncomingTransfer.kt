package app.synco.transfer

import app.synco.protocol.framing.BlobChunk
import app.synco.protocol.message.TransferStart
import java.io.BufferedOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID

class IncomingTransfer(
    val transferId: UUID,
    val start: TransferStart,
    private val storage: TransferStorage,
    private val relativePath: String? = null,
) : Closeable {

    private val digest = Sha256.newDigest()
    private val staging: File = storage.stagingFile(transferId)

    private var sink: OutputStream? = null
    private var written = 0L
    private var finished = false

    val bytesWritten: Long get() = written

    val expectedSize: Long get() = start.size

    val name: String get() = start.name

    fun accept(chunk: BlobChunk): TransferFailure? {
        if (finished) return TransferFailure.CLOSED
        if (chunk.transferId != transferId) return TransferFailure.UNKNOWN_TRANSFER
        if (chunk.offset != written) return TransferFailure.OFFSET_MISMATCH
        if (written + chunk.data.size > start.size) return TransferFailure.TOO_LARGE
        return runCatching {
            openSink().write(chunk.data)
            digest.update(chunk.data)
            written += chunk.data.size
            null
        }.getOrElse { TransferFailure.WRITE_FAILED }
    }

    fun complete(): TransferOutcome {
        if (finished) return TransferOutcome.Failed(transferId, TransferFailure.CLOSED)
        if (written != start.size) return fail(TransferFailure.INCOMPLETE)
        val computed = runCatching {
            openSink()
            close()
            Sha256.hexOf(digest)
        }.getOrNull() ?: return fail(TransferFailure.WRITE_FAILED)
        if (!computed.equals(start.sha256, ignoreCase = true)) return fail(TransferFailure.HASH_MISMATCH)
        val destination = runCatching { promote() }.getOrNull() ?: return fail(TransferFailure.WRITE_FAILED)
        finished = true
        return TransferOutcome.Completed(transferId, destination, destination.name, start.mime, written, computed)
    }

    fun fail(failure: TransferFailure): TransferOutcome.Failed {
        close()
        staging.delete()
        finished = true
        return TransferOutcome.Failed(transferId, failure)
    }

    override fun close() {
        runCatching { sink?.close() }
        sink = null
    }

    private fun openSink(): OutputStream =
        sink ?: BufferedOutputStream(FileOutputStream(staging)).also { sink = it }

    private fun promote(): File {
        val destination = storage.createReceivedFile(start.name, relativePath)
        if (staging.renameTo(destination)) return destination
        staging.copyTo(destination, overwrite = true)
        staging.delete()
        return destination
    }
}
