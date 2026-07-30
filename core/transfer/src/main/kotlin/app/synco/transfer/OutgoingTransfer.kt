package app.synco.transfer

import android.content.ContentResolver
import app.synco.protocol.ProtocolConstants
import app.synco.protocol.framing.BlobChunk
import app.synco.protocol.message.TransferStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.UUID

class OutgoingTransfer private constructor(
    val transferId: UUID,
    val clipId: String,
    val source: TransferSource,
    val sha256: String,
    val size: Long,
    private val resolver: ContentResolver,
) {
    val name: String get() = source.name

    val mime: String get() = source.mime

    val start: TransferStart
        get() = TransferStart(transferId.toString(), clipId, name, mime, size, sha256)

    fun chunks(): Flow<BlobChunk> = flow {
        source.openStream(resolver).use { input ->
            val buffer = ByteArray(ProtocolConstants.MAX_BLOB_CHUNK_BYTES)
            var offset = 0L
            while (offset < size) {
                val wanted = minOf(buffer.size.toLong(), size - offset).toInt()
                val read = input.read(buffer, 0, wanted)
                if (read <= 0) break
                emit(BlobChunk(transferId, offset, buffer.copyOf(read)))
                offset += read
            }
        }
    }.flowOn(Dispatchers.IO)

    companion object {
        suspend fun prepare(
            resolver: ContentResolver,
            clipId: String,
            source: TransferSource,
            transferId: UUID = TransferIds.newId(),
        ): OutgoingTransfer {
            val digest = withContext(Dispatchers.IO) { source.openStream(resolver).use { Sha256.of(it) } }
            return OutgoingTransfer(transferId, clipId, source, digest.sha256, digest.size, resolver)
        }

        fun fromStaged(
            resolver: ContentResolver,
            clipId: String,
            staged: StagedBlob,
            name: String,
            mime: String,
            transferId: UUID,
        ): OutgoingTransfer = OutgoingTransfer(
            transferId = transferId,
            clipId = clipId,
            source = TransferSource.Local(staged.file, mime, name),
            sha256 = staged.sha256,
            size = staged.size,
            resolver = resolver,
        )
    }
}
