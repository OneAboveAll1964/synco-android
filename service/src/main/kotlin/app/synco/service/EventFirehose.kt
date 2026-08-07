package app.synco.service

import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent

internal object EventFirehose {

    private const val TAG = "SyncoFirehose"

    val isEnabled: Boolean get() = Log.isLoggable(TAG, Log.DEBUG)

    fun widen(info: AccessibilityServiceInfo): AccessibilityServiceInfo {
        if (isEnabled) info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        return info
    }

    fun log(event: AccessibilityEvent?) {
        if (!isEnabled || event == null) return
        Log.d(
            TAG,
            AccessibilityEvent.eventTypeToString(event.eventType) +
                " pkg=" + event.packageName +
                " class=" + event.className +
                " text=" + event.text.joinToString().take(60),
        )
    }
}
