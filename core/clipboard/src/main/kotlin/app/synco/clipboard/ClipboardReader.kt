package app.synco.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import app.synco.logging.SyncoLog
import app.synco.protocol.ProtocolConstants
import app.synco.protocol.clip.ClipHash
import app.synco.protocol.message.ClipRepKind
import app.synco.transfer.ContentUriMetadata

class ClipboardReader(
    private val clipboardManager: ClipboardManager,
    blobs: BlobRepFactory,
    metadata: ContentUriMetadata,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val items = ClipItemReader(blobs, metadata)

    fun clipTimestamp(): Long? =
        runCatching { clipboardManager.primaryClipDescription?.timestamp }.getOrNull()

    suspend fun read(
        clipId: String,
        maxBlobBytes: Long = ProtocolConstants.DEFAULT_MAX_BLOB_BYTES,
    ): ClipboardSnapshot? {
        val clip = runCatching { clipboardManager.primaryClip }
            .onFailure { SyncoLog.clipboard.warn("could not read the primary clip", it) }
            .getOrNull()
        if (clip == null) {
            SyncoLog.clipboard.warn("the primary clip was unavailable, the clipboard may not be readable yet")
            return null
        }
        return readClip(clip, clipId, maxBlobBytes)
    }

    suspend fun readClip(
        clip: ClipData,
        clipId: String,
        maxBlobBytes: Long = ProtocolConstants.DEFAULT_MAX_BLOB_BYTES,
    ): ClipboardSnapshot? {
        val prepared = mutableListOf<PreparedRep>()
        for (index in 0 until clip.itemCount) {
            prepared += items.reps(clipId, clip.getItemAt(index), maxBlobBytes)
        }
        val ordered = RepOrder.sorted(dedupeInline(prepared))
        if (ordered.isEmpty()) {
            SyncoLog.clipboard.warn(
                "a clip with ${clip.itemCount} item(s) produced no usable representation",
            )
            return null
        }
        val reps = ordered.map { it.rep }
        return ClipboardSnapshot(
            clipId = clipId,
            reps = reps,
            hash = ClipHash.compute(reps),
            transfers = ordered.mapNotNull { it.transfer },
            capturedAtMillis = clock(),
        )
    }

    private fun dedupeInline(prepared: List<PreparedRep>): List<PreparedRep> {
        val seen = mutableSetOf<String>()
        return prepared.filter { it.rep.kind in ClipRepKind.STREAMED || seen.add(it.rep.kind) }
    }
}
