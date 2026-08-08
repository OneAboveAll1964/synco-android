package app.synco.sync

import app.synco.protocol.message.ClipRep
import java.io.File

internal class HistoryRecordingSink(
    private val delegate: ClipboardSink,
    private val history: ClipHistoryRecorder,
) : ClipboardSink {

    override fun apply(clipHash: String, reps: List<ClipRep>, blobs: Map<String, File>): Boolean {
        val applied = delegate.apply(clipHash, reps, blobs)
        if (applied) history.peer(reps)
        return applied
    }
}
