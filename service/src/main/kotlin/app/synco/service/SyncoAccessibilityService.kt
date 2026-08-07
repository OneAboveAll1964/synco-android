package app.synco.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import app.synco.clipboard.CaptureRoute
import app.synco.clipboard.ClipboardCapture
import app.synco.clipboard.CopyIntentDetector
import app.synco.logging.SyncoLog
import app.synco.sync.CaptureTuningHolder
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

    private var tuning: CaptureTuningHolder? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        val tuning = syncoGraphOrNull()?.captureTuning
        this.tuning = tuning
        gate = FocusGate(this) { tuning?.focusTimeoutMillis() ?: DEFAULT_FOCUS_TIMEOUT }
        val excluded = InputMethodPackages.of(this)
        val keyboards = InputMethodPackages.keyboardsOf(this)
        detector = CopyIntentDetector(
            ownPackageName = packageName,
            excludedPackages = { excluded },
            keyboardPackages = { keyboards },
            gestureWindowMillis = { tuning?.gestureWindowMillis() ?: DEFAULT_GESTURE_WINDOW },
            attemptsPerGesture = { tuning?.attemptsPerGesture() ?: DEFAULT_ATTEMPTS },
        )
        if (EventFirehose.isEnabled) {
            serviceInfo = EventFirehose.widen(serviceInfo)
        }
        capture = syncoGraphOrNull()?.clipboard?.also { it.setAccessibilityConnected(true) }
        FocusGateHolder.install(this)
        SyncoLog.clipboard.info("focus gate is ready")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        EventFirehose.log(event)
        val detector = detector ?: return
        if (!isCapturing()) return
        val signal = AccessibilitySignals.of(event) ?: return
        val triggered = detector.observe(signal)
        SyncoLog.clipboard.info(
            "signal ${signal.kind} from ${signal.packageName ?: "?"} triggered=$triggered",
        )
        if (!triggered) return
        scope.launch { captureThroughFocus() }
    }

    override suspend fun captureThroughFocus() {
        if (!isCapturing()) return
        val gate = gate ?: return
        val capture = capture ?: return
        val captured = captureLock.withLock {
            SyncoLog.clipboard.info("opening the focus overlay to read the clip")
            gate.withFocus { capture.captureVia(CaptureRoute.ACCESSIBILITY_FOCUS_GATE) } == true
        }
        if (captured) detector?.captured()
    }

    private fun isCapturing(): Boolean = CaptureOwner.accessibilityShouldCapture(
        syncIsOn = syncIsOn(),
        mode = tuning?.mode(),
        state = ShizukuStatus.state.value,
    )

    private fun syncIsOn(): Boolean {
        val state = syncoGraphOrNull()?.state?.value ?: return CaptureSwitch.isOn.value
        return state.running && !state.paused
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

    private companion object {
        const val DEFAULT_FOCUS_TIMEOUT = 800L
        const val DEFAULT_GESTURE_WINDOW = 8_000L
        const val DEFAULT_ATTEMPTS = 2
    }

    private fun release() {
        FocusGateHolder.remove(this)
        capture?.setAccessibilityConnected(false)
    }
}
