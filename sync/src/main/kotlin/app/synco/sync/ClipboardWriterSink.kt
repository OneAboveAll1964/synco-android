package app.synco.sync

import app.synco.clipboard.ClipboardWriter
import app.synco.protocol.message.ClipRep
import java.io.File

class ClipboardWriterSink(private val writer: ClipboardWriter) : ClipboardSink {

    override fun apply(clipHash: String, reps: List<ClipRep>, blobs: Map<String, File>): Boolean =
        writer.apply(clipHash, reps, blobs)
}
