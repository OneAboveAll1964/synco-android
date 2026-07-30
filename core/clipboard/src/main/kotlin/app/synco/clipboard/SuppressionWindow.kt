package app.synco.clipboard

import app.synco.protocol.ProtocolConstants

class SuppressionWindow(
    private val windowMillis: Long = ProtocolConstants.SUPPRESSION_WINDOW_MILLIS,
    private val maxEntries: Int = ProtocolConstants.SUPPRESSION_MAX_ENTRIES,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val entries = LinkedHashMap<String, Long>()

    val size: Int
        get() = synchronized(entries) {
            prune()
            entries.size
        }

    fun record(hash: String) {
        synchronized(entries) {
            prune()
            entries.remove(hash)
            entries[hash] = clock()
            while (entries.size > maxEntries) {
                val oldest = entries.keys.firstOrNull() ?: break
                entries.remove(oldest)
            }
        }
    }

    fun recordAll(hashes: Collection<String>) {
        hashes.forEach { record(it) }
    }

    fun consume(hash: String): Boolean = synchronized(entries) {
        prune()
        entries.remove(hash) != null
    }

    fun contains(hash: String): Boolean = synchronized(entries) {
        prune()
        entries.containsKey(hash)
    }

    fun clear() {
        synchronized(entries) { entries.clear() }
    }

    private fun prune() {
        val cutoff = clock() - windowMillis
        val stale = entries.entries.takeWhile { it.value < cutoff }.map { it.key }
        stale.forEach { entries.remove(it) }
    }
}
