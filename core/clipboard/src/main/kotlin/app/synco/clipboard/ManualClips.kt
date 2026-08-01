package app.synco.clipboard

import android.net.Uri
import app.synco.protocol.ProtocolConstants
import app.synco.protocol.clip.ClipHash
import app.synco.protocol.message.ClipRep
import app.synco.transfer.ContentUriMetadata
import java.util.UUID

class ManualClips(
    private val blobs: BlobRepFactory,
    private val metadata: ContentUriMetadata,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun text(value: String): ClipboardSnapshot? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null
        val reps = listOf<ClipRep>(ClipRep.Text(trimmed))
        return ClipboardSnapshot(
            clipId = UUID.randomUUID().toString(),
            reps = reps,
            hash = ClipHash.compute(reps),
            transfers = emptyList(),
            capturedAtMillis = clock(),
        )
    }

    suspend fun file(
        uri: Uri,
        maxBlobBytes: Long = ProtocolConstants.DEFAULT_MAX_BLOB_BYTES,
    ): ClipboardSnapshot? {
        val clipId = UUID.randomUUID().toString()
        val prepared = blobs.fromUri(clipId, uri, maxBlobBytes) ?: return null
        val reps = listOf(prepared.rep)
        return ClipboardSnapshot(
            clipId = clipId,
            reps = reps,
            hash = ClipHash.compute(reps),
            transfers = listOfNotNull(prepared.transfer),
            capturedAtMillis = clock(),
        )
    }
}
