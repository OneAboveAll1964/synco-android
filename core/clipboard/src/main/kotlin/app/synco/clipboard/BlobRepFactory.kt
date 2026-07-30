package app.synco.clipboard

import android.net.Uri
import app.synco.logging.SyncoLog
import app.synco.protocol.message.ClipRep
import app.synco.transfer.ContentUriMetadata
import app.synco.transfer.OutgoingTransfer
import app.synco.transfer.TransferIds
import app.synco.transfer.TransferManager
import app.synco.transfer.TransferSource
import app.synco.transfer.TransferStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BlobRepFactory(
    private val metadata: ContentUriMetadata,
    private val storage: TransferStorage,
    private val transfers: TransferManager,
) {
    suspend fun fromUri(clipId: String, uri: Uri, maxBlobBytes: Long): PreparedRep? {
        val resolved = metadata.resolve(uri)
        if (resolved.hasKnownSize && resolved.size > maxBlobBytes) {
            SyncoLog.clipboard.warn(
                "dropped a ${resolved.mime} clip of ${resolved.size} bytes, over the cap of $maxBlobBytes",
            )
            return null
        }
        val transfer = transfers.stageOutgoing(
            clipId = clipId,
            source = TransferSource.content(uri, resolved),
            peerMaxBlobBytes = maxBlobBytes,
        )
        if (transfer == null) {
            SyncoLog.clipboard.warn("dropped a ${resolved.mime} clip, its blob could not be staged")
            return null
        }
        SyncoLog.clipboard.info("captured a ${resolved.mime} clip of ${transfer.size} bytes")
        return PreparedRep(repOf(transfer, resolved.isImage), transfer)
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
