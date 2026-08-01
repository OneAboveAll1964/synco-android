package app.synco.clipboard

class CopyIntentDetector(
    private val ownPackageName: String?,
    private val excludedPackages: () -> Set<String> = ::emptySet,
    private val gestureWindowMillis: () -> Long = { DEFAULT_GESTURE_WINDOW_MILLIS },
    private val minIntervalMillis: Long = DEFAULT_MIN_INTERVAL_MILLIS,
    private val attemptsPerGesture: () -> Int = { DEFAULT_ATTEMPTS_PER_GESTURE },
) {
    private var armedAtMillis: Long? = null

    private var attemptsLeft = 0

    private var lastAttemptAtMillis: Long? = null

    fun observe(signal: CopySignal): Boolean = when (signal.kind) {
        CopySignalKind.CLICK, CopySignalKind.LONG_CLICK, CopySignalKind.TEXT_SELECTION_CHANGED ->
            arm(signal)
        CopySignalKind.WINDOW_STATE_CHANGED -> onWindowStateChanged(signal)
    }

    fun captured() {
        armedAtMillis = null
        attemptsLeft = 0
    }

    private fun arm(signal: CopySignal): Boolean {
        if (isOurs(signal) || isExcluded(signal)) return false
        armedAtMillis = signal.timestampMillis
        attemptsLeft = attemptsPerGesture()
        return false
    }

    private fun onWindowStateChanged(signal: CopySignal): Boolean {
        if (isOurs(signal) || isExcluded(signal)) return false
        val armed = armedAtMillis ?: return false
        if (signal.timestampMillis - armed !in 0..gestureWindowMillis()) return false
        if (attemptsLeft <= 0) return false
        if (tooSoon(signal.timestampMillis)) return false
        attemptsLeft -= 1
        lastAttemptAtMillis = signal.timestampMillis
        return true
    }

    private fun tooSoon(nowMillis: Long): Boolean {
        val last = lastAttemptAtMillis ?: return false
        return nowMillis - last in 0 until minIntervalMillis
    }

    private fun isOurs(signal: CopySignal): Boolean = signal.packageName == ownPackageName

    private fun isExcluded(signal: CopySignal): Boolean = signal.packageName in excludedPackages()

    companion object {
        const val DEFAULT_GESTURE_WINDOW_MILLIS = 8_000L
        const val DEFAULT_MIN_INTERVAL_MILLIS = 400L
        const val DEFAULT_ATTEMPTS_PER_GESTURE = 2
    }
}
