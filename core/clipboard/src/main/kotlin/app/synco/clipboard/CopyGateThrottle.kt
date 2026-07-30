package app.synco.clipboard

class CopyGateThrottle(
    private val minIntervalMillis: Long = DEFAULT_MIN_INTERVAL_MILLIS,
) {
    private var lastOpenedAtMillis: Long? = null

    fun accept(nowMillis: Long): Boolean {
        val last = lastOpenedAtMillis
        if (last != null && nowMillis - last < minIntervalMillis) return false
        lastOpenedAtMillis = nowMillis
        return true
    }

    companion object {
        const val DEFAULT_MIN_INTERVAL_MILLIS = 600L
    }
}
