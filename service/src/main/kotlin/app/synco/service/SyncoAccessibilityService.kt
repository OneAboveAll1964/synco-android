package app.synco.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import app.synco.clipboard.CaptureRoute
import app.synco.clipboard.ClipboardCapture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SyncoAccessibilityService : AccessibilityService(), GatedCapture {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val captureLock = Mutex()

    private var gate: FocusGate? = null

    private var capture: ClipboardCapture? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        gate = FocusGate(this)
        capture = syncoGraphOrNull()?.clipboard?.also { it.setAccessibilityConnected(true) }
        FocusGateHolder.install(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override suspend fun captureThroughFocus() {
        val gate = gate ?: return
        val capture = capture ?: return
        captureLock.withLock {
            gate.withFocus { capture.captureVia(CaptureRoute.ACCESSIBILITY_FOCUS_GATE) }
        }
    }

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        release()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        release()
        scope.cancel()
        super.onDestroy()
    }

    private fun release() {
        FocusGateHolder.remove(this)
        capture?.setAccessibilityConnected(false)
    }
}
