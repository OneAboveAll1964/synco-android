package app.synco.clipboard

import android.net.Uri
import app.synco.logging.SyncoLog
import app.synco.protocol.message.ClipRep
import app.synco.transfer.ContentMetadata
import app.synco.transfer.ContentUriMetadata
import app.synco.transfer.OutgoingTransfer
import app.synco.transfer.TransferIds
import app.synco.transfer.TransferManager
import app.synco.transfer.TransferSource
import app.synco.transfer.TransferStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class BlobRepFactory(
    private val metadata: ContentUriMetadata,
    private val storage: TransferStorage,
    private val transfers: TransferManager,
) {
    suspend fun fromUri(clipId: String, uri: Uri, maxBlobBytes: Long): PreparedRep? = coroutineScope {
        val transferId = TransferIds.newId()
        val facts = async { metadata.resolve(uri) }
        val bytes = async {
            transfers.stageBytes(
                transferId = transferId,
                source = TransferSource.content(uri, ContentMetadata.unknown()),
                peerMaxBlobBytes = maxBlobBytes,
            )
        }
        val resolved = facts.await()
        val staged = bytes.await()
        if (staged == null) {
            SyncoLog.clipboard.warn("dropped a ${resolved.mime} clip, its blob could not be staged")
            return@coroutineScope null
        }
        val transfer = transfers.adoptStaged(
            clipId = clipId,
            staged = staged,
            name = resolved.name,
            mime = resolved.mime,
            transferId = transferId,
        )
        SyncoLog.clipboard.info("captured a ${resolved.mime} clip of ${transfer.size} bytes")
        PreparedRep(repOf(transfer, resolved.isImage), transfer)
    }

    suspend fun fromBytes(
        clipId: String,
        name: String,
        mime: String,
        bytes: ByteArray,
        maxBlobBytes: Long,
    ): PreparedRep? {
        if (bytes.size > maxBlobBytes) return null
        val transferId = TransferIds.newId()
        val staged = withContext(Dispatchers.IO) {
            runCatching { storage.stagingFile(transferId).also { it.writeBytes(bytes) } }.getOrNull()
        } ?: return null
        val transfer = transfers.prepareOutgoing(
            clipId = clipId,
            source = TransferSource.Local(staged, mime, name),
            peerMaxBlobBytes = maxBlobBytes,
            transferId = transferId,
        ) ?: return null
        return PreparedRep(repOf(transfer, isImage = false), transfer)
    }

    private fun repOf(transfer: OutgoingTransfer, isImage: Boolean): ClipRep = if (isImage) {
        ClipRep.Image(
            mime = transfer.mime,
            name = transfer.name,
            size = transfer.size,
            sha256 = transfer.sha256,
            transferId = transfer.transferId.toString(),
        )
    } else {
        ClipRep.File(
            mime = transfer.mime,
            name = transfer.name,
            size = transfer.size,
            sha256 = transfer.sha256,
            transferId = transfer.transferId.toString(),
        )
    }
}
