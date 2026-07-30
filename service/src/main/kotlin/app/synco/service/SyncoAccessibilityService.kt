package app.synco.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import app.synco.clipboard.CaptureRoute
import app.synco.clipboard.ClipboardCapture
import app.synco.clipboard.CopyIntentDetector
import app.synco.logging.SyncoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SyncoAccessibilityService : AccessibilityService(), GatedCapture {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val captureLock = Mutex()

    private var gate: FocusGate? = null

    private var capture: ClipboardCapture? = null

    private var detector: CopyIntentDetector? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        gate = FocusGate(this)
        val keyboards = InputMethodPackages.of(this)
        detector = CopyIntentDetector(ownPackageName = packageName, excludedPackages = { keyboards })
        capture = syncoGraphOrNull()?.clipboard?.also { it.setAccessibilityConnected(true) }
        FocusGateHolder.install(this)
        SyncoLog.clipboard.info("focus gate is ready")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val detector = detector ?: return
        val signal = AccessibilitySignals.of(event) ?: return
        if (!detector.observe(signal)) return
        scope.launch { captureThroughFocus() }
    }

    override suspend fun captureThroughFocus() {
        val gate = gate ?: return
        val capture = capture ?: return
        captureLock.withLock {
            SyncoLog.clipboard.info("opening the focus overlay to read the clip")
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
