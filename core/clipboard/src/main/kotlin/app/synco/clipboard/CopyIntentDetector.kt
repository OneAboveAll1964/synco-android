package app.synco.clipboard

class CopyIntentDetector(
    private val ownPackageName: String?,
    private val excludedPackages: () -> Set<String> = ::emptySet,
    private val gestureWindowMillis: Long = DEFAULT_GESTURE_WINDOW_MILLIS,
) {
    private var pendingGestureAtMillis: Long? = null

    fun observe(signal: CopySignal): Boolean = when (signal.kind) {
        CopySignalKind.CLICK, CopySignalKind.LONG_CLICK, CopySignalKind.TEXT_SELECTION_CHANGED ->
            rememberGesture(signal)
        CopySignalKind.WINDOW_STATE_CHANGED -> onWindowStateChanged(signal)
    }

    private fun rememberGesture(signal: CopySignal): Boolean {
        if (isOurs(signal) || isExcluded(signal)) return false
        pendingGestureAtMillis = signal.timestampMillis
        return false
    }

    private fun onWindowStateChanged(signal: CopySignal): Boolean {
        if (isOurs(signal) || isExcluded(signal)) return false
        val gesture = pendingGestureAtMillis ?: return false
        if (signal.timestampMillis - gesture !in 0..gestureWindowMillis) return false
        pendingGestureAtMillis = null
        return true
    }

    private fun isOurs(signal: CopySignal): Boolean = signal.packageName == ownPackageName

    private fun isExcluded(signal: CopySignal): Boolean = signal.packageName in excludedPackages()

    companion object {
        const val DEFAULT_GESTURE_WINDOW_MILLIS = 2_000L
    }
}
