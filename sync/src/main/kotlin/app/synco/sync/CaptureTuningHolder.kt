package app.synco.sync

import app.synco.storage.CaptureMode
import app.synco.storage.CaptureTuning
import app.synco.storage.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CaptureTuningHolder(settings: SettingsStore, scope: CoroutineScope) {

    @Volatile
    private var tuning = CaptureTuning.DEFAULT

    @Volatile
    private var mode = CaptureMode.DEFAULT

    init {
        settings.captureTuning.onEach { tuning = it }.launchIn(scope)
        settings.captureMode.onEach { mode = it }.launchIn(scope)
    }

    fun waitMillis(): Long = tuning.waitMillis

    fun focusTimeoutMillis(): Long = tuning.focusTimeoutMillis

    fun attemptsPerGesture(): Int = tuning.attemptsPerGesture

    fun gestureWindowMillis(): Long = tuning.gestureWindowMillis

    fun mode(): CaptureMode = mode
}
