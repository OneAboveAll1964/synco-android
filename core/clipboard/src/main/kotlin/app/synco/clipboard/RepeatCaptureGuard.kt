package app.synco.clipboard

class RepeatCaptureGuard(
    private val windowMillis: Long = DEFAULT_WINDOW_MILLIS,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private var lastHash: String? = null
    private var lastStampMillis: Long? = null
    private var lastCapturedAtMillis = 0L

    @Synchronized
    fun isRepeat(hash: String, stampMillis: Long?): Boolean {
        val now = clock()
        if (hash != lastHash) {
            remember(hash, stampMillis, now)
            return false
        }
        val repeat = if (stampMillis != null && lastStampMillis != null) {
            stampMillis == lastStampMillis
        } else {
            now - lastCapturedAtMillis <= windowMillis
        }
        if (!repeat) remember(hash, stampMillis, now)
        return repeat
    }

    @Synchronized
    fun reset() {
        lastHash = null
        lastStampMillis = null
        lastCapturedAtMillis = 0L
    }

    private fun remember(hash: String, stampMillis: Long?, atMillis: Long) {
        lastHash = hash
        lastStampMillis = stampMillis
        lastCapturedAtMillis = atMillis
    }

    companion object {
        const val DEFAULT_WINDOW_MILLIS = 4_000L
    }
}
