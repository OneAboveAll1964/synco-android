package app.synco.service

import android.view.accessibility.AccessibilityEvent
import app.synco.clipboard.CopySignal
import app.synco.clipboard.CopySignalKind
import app.synco.logging.SyncoLog

internal object AccessibilitySignals {

    fun of(event: AccessibilityEvent?): CopySignal? {
        event ?: return null
        val signal = when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> clickSignal(event, CopySignalKind.CLICK)
            AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> clickSignal(event, CopySignalKind.LONG_CLICK)
            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> CopySignal(
                kind = CopySignalKind.TEXT_SELECTION_CHANGED,
                timestampMillis = event.eventTime,
                packageName = event.packageName?.toString(),
                className = event.className?.toString(),
            )
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> CopySignal(
                kind = CopySignalKind.WINDOW_STATE_CHANGED,
                timestampMillis = event.eventTime,
                packageName = event.packageName?.toString(),
                className = event.className?.toString(),
            )
            else -> null
        }
        signal?.let { trace(it) }
        return signal
    }

    private fun trace(signal: CopySignal) {
        SyncoLog.clipboard.debug {
            "signal ${signal.kind} from ${signal.packageName ?: "?"} " +
                "class=${signal.className ?: "?"} labelled=${signal.text != null || signal.contentDescription != null}"
        }
    }

    private fun clickSignal(event: AccessibilityEvent, kind: CopySignalKind): CopySignal =
        CopySignal(
            kind = kind,
            timestampMillis = event.eventTime,
            packageName = event.packageName?.toString(),
            className = event.className?.toString(),
            text = event.text.firstOrNull { !it.isNullOrBlank() }?.toString(),
            contentDescription = event.contentDescription?.toString(),
        )
}
