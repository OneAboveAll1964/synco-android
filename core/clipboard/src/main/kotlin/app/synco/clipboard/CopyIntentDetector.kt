package app.synco.clipboard

class CopyIntentDetector(
    private val labels: CopyLabels,
    private val ownPackageName: String?,
    private val selectionWindowMillis: Long = DEFAULT_SELECTION_WINDOW_MILLIS,
) {
    private var lastSelectionAtMillis: Long? = null

    fun observe(signal: CopySignal): Boolean = when (signal.kind) {
        CopySignalKind.CLICK, CopySignalKind.LONG_CLICK -> onClick(signal)
        CopySignalKind.TEXT_SELECTION_CHANGED -> onSelectionChanged(signal)
        CopySignalKind.WINDOW_STATE_CHANGED -> onWindowStateChanged(signal)
    }

    private fun onClick(signal: CopySignal): Boolean {
        if (labels.matches(signal.text) || labels.matches(signal.contentDescription)) {
            lastSelectionAtMillis = null
            return true
        }
        if (followsRecentSelection(signal.timestampMillis)) {
            lastSelectionAtMillis = null
            return true
        }
        return false
    }

    private fun onSelectionChanged(signal: CopySignal): Boolean {
        lastSelectionAtMillis = signal.timestampMillis
        return false
    }

    private fun onWindowStateChanged(signal: CopySignal): Boolean {
        lastSelectionAtMillis = null
        return signal.packageName != ownPackageName
    }

    private fun followsRecentSelection(nowMillis: Long): Boolean {
        val last = lastSelectionAtMillis ?: return false
        return nowMillis - last in 0..selectionWindowMillis
    }

    companion object {
        const val DEFAULT_SELECTION_WINDOW_MILLIS = 1_200L
    }
}
