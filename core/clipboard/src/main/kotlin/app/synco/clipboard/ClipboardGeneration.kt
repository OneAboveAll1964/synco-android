package app.synco.clipboard

class ClipboardGeneration(
    private val fallbackWindowMillis: Long = DEFAULT_FALLBACK_WINDOW_MILLIS,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private var stampMillis: Long? = null
    private var hashes: Set<String> = emptySet()
    private var recordedAtMillis = 0L

    @Synchronized
    fun applied(knownHashes: Set<String>, atStampMillis: Long?) {
        remember(knownHashes, atStampMillis)
    }

    @Synchronized
    fun captured(hash: String, atStampMillis: Long?) {
        remember(setOf(hash), atStampMillis)
    }

    @Synchronized
    fun isKnown(hash: String, atStampMillis: Long?): Boolean {
        val current = stampMillis
        if (atStampMillis != null && current != null) return atStampMillis == current
        return hash in hashes && clock() - recordedAtMillis <= fallbackWindowMillis
    }

    @Synchronized
    fun reset() {
        remember(emptySet(), null)
    }

    private fun remember(knownHashes: Set<String>, atStampMillis: Long?) {
        hashes = knownHashes
        stampMillis = atStampMillis
        recordedAtMillis = clock()
    }

    companion object {
        const val DEFAULT_FALLBACK_WINDOW_MILLIS = 10_000L
    }
}
