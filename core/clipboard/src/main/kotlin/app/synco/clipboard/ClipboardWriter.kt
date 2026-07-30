package app.synco.clipboard

import android.content.ClipboardManager
import app.synco.protocol.message.ClipRep
import java.io.File

class ClipboardWriter(
    private val clipboardManager: ClipboardManager,
    private val clipData: ClipDataBuilder,
    private val suppression: SuppressionWindow,
) {
    fun apply(clipHash: String, reps: List<ClipRep>, blobs: Map<String, File>): Boolean {
        val built = clipData.build(reps, blobs) ?: return false
        suppression.record(clipHash)
        suppression.recordAll(built.echoHashes)
        return runCatching { clipboardManager.setPrimaryClip(built.data) }.isSuccess
    }
}
