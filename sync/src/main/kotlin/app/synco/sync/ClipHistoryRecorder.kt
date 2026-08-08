package app.synco.sync

import app.synco.protocol.message.ClipRep
import app.synco.storage.ClipHistoryEntry
import app.synco.storage.ClipHistoryStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ClipHistoryRecorder(
    private val store: ClipHistoryStore,
    private val scope: CoroutineScope,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun local(reps: List<ClipRep>) = record(reps, fromPeer = false)

    fun peer(reps: List<ClipRep>) = record(reps, fromPeer = true)

    private fun record(reps: List<ClipRep>, fromPeer: Boolean) {
        val entry = entryOf(reps, fromPeer) ?: return
        scope.launch { store.add(entry) }
    }

    private fun entryOf(reps: List<ClipRep>, fromPeer: Boolean): ClipHistoryEntry? {
        val url = reps.filterIsInstance<ClipRep.Url>().firstOrNull()
        if (url != null) {
            return ClipHistoryEntry(url.url, isUrl = true, atMillis = clock(), fromPeer = fromPeer)
        }
        val text = reps.filterIsInstance<ClipRep.Text>().firstOrNull() ?: return null
        return ClipHistoryEntry(text.text, isUrl = false, atMillis = clock(), fromPeer = fromPeer)
    }
}
