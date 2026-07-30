package app.synco.sync

import app.synco.protocol.message.ClipRep
import java.io.File

interface ClipboardSink {
    fun apply(clipHash: String, reps: List<ClipRep>, blobs: Map<String, File>): Boolean
}
