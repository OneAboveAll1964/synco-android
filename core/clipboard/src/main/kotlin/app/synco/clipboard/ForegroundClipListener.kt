package app.synco.clipboard

import android.content.ClipboardManager
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce

class ForegroundClipListener(
    private val clipboardManager: ClipboardManager,
    private val debounceMillis: Long = DEFAULT_DEBOUNCE_MILLIS,
    private val onForegroundChange: suspend () -> Unit,
) {
    suspend fun run() {
        val triggers = MutableSharedFlow<Unit>(
            extraBufferCapacity = TRIGGER_BUFFER,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
        val listener = ClipboardManager.OnPrimaryClipChangedListener { triggers.tryEmit(Unit) }
        clipboardManager.addPrimaryClipChangedListener(listener)
        try {
            triggers.debounce(debounceMillis).collect { onForegroundChange() }
        } finally {
            clipboardManager.removePrimaryClipChangedListener(listener)
        }
    }

    private companion object {
        const val DEFAULT_DEBOUNCE_MILLIS = 150L
        const val TRIGGER_BUFFER = 8
    }
}
