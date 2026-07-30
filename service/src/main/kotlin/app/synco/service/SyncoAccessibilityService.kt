package app.synco.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import app.synco.clipboard.CaptureRoute
import app.synco.clipboard.ClipboardCapture
import app.synco.clipboard.CopyGateThrottle
import app.synco.clipboard.CopyIntentDetector
import app.synco.logging.SyncoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SyncoAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val throttle = CopyGateThrottle()

    private var gate: FocusGate? = null

    private var detector: CopyIntentDetector? = null

    private var capture: ClipboardCapture? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        gate = FocusGate(this)
        detector = CopyIntentDetector(CopyLabelResolver.resolve(this), packageName)
        val graph = syncoGraphOrNull()
        if (graph == null) SyncoLog.clipboard.warn("the accessibility service connected before the graph existed")
        capture = graph?.clipboard?.also { it.setAccessibilityConnected(true) }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val detector = detector ?: return
        val signal = AccessibilitySignals.of(event) ?: return
        if (!detector.observe(signal)) return
        if (!throttle.accept(signal.timestampMillis)) {
            SyncoLog.clipboard.debug { "throttled a copy signal ${signal.kind}" }
            return
        }
        SyncoLog.clipboard.info("copy detected from ${signal.packageName ?: "?"} via ${signal.kind}")
        captureThroughFocus()
    }

    private fun captureThroughFocus() {
        val gate = gate ?: return
        val capture = capture ?: return
        scope.launch {
            gate.withFocus { capture.captureVia(CaptureRoute.ACCESSIBILITY_FOCUS_GATE) }
        }
    }

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        capture?.setAccessibilityConnected(false)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        capture?.setAccessibilityConnected(false)
        scope.cancel()
        super.onDestroy()
    }

}
