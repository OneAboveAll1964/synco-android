package app.synco.service

import android.view.accessibility.AccessibilityEvent
import app.synco.clipboard.CopySignal
import app.synco.clipboard.CopySignalKind

internal object AccessibilitySignals {

    fun of(event: AccessibilityEvent?): CopySignal? {
        event ?: return null
        val kind = when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> CopySignalKind.CLICK
            AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> CopySignalKind.LONG_CLICK
            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> CopySignalKind.TEXT_SELECTION_CHANGED
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> CopySignalKind.WINDOW_STATE_CHANGED
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED ->
                if (isToast(event)) CopySignalKind.COPY_TOAST else return null
            else -> return null
        }
        return CopySignal(kind, event.eventTime, event.packageName?.toString())
    }

    private fun isToast(event: AccessibilityEvent): Boolean =
        event.className?.toString() == TOAST_CLASS

    private const val TOAST_CLASS = "android.widget.Toast"
}
