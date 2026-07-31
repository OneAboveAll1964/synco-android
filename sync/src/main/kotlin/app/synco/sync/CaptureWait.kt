package app.synco.sync

import app.synco.storage.CaptureWaitChoice
import app.synco.storage.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CaptureWait(settings: SettingsStore, scope: CoroutineScope) {

    @Volatile
    private var current = CaptureWaitChoice.DEFAULT.millis

    init {
        settings.captureWaitMillis.onEach { current = it }.launchIn(scope)
    }

    fun millis(): Long = current
}
