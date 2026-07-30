package app.synco.service

import java.util.concurrent.atomic.AtomicReference

object FocusGateHolder {

    private val current = AtomicReference<GatedCapture?>(null)

    val isReady: Boolean get() = current.get() != null

    fun install(capture: GatedCapture) {
        current.set(capture)
    }

    fun remove(capture: GatedCapture) {
        current.compareAndSet(capture, null)
    }

    suspend fun captureNow() {
        current.get()?.captureThroughFocus()
    }
}
