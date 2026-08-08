package app.synco.clipboard

import android.content.ClipboardManager
import app.synco.protocol.message.ClipRep
import java.io.File

class ClipboardWriter(
    private val clipboardManager: ClipboardManager,
    private val clipData: ClipDataBuilder,
    private val generation: ClipboardGeneration,
) {
    fun apply(clipHash: String, reps: List<ClipRep>, blobs: Map<String, File>): Boolean {
        val built = clipData.build(reps, blobs) ?: return false
        val written = runCatching { clipboardManager.setPrimaryClip(built.data) }.isSuccess
        if (written) generation.applied(built.echoHashes + clipHash, stampOfWrite())
        return written
    }

    private fun stampOfWrite(): Long? =
        runCatching { clipboardManager.primaryClipDescription?.timestamp }
            .getOrNull()
            ?.takeIf { it > 0 }
}
