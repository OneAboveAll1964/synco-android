package app.synco.sync

import app.synco.protocol.message.ClipRep
import java.io.File

internal class RecordingClipboard(private val accepts: Boolean = true) : ClipboardSink {

    val writes = mutableListOf<Write>()

    override fun apply(clipHash: String, reps: List<ClipRep>, blobs: Map<String, File>): Boolean {
        writes += Write(clipHash, reps, blobs)
        return accepts
    }

    class Write(val clipHash: String, val reps: List<ClipRep>, val blobs: Map<String, File>)
}
