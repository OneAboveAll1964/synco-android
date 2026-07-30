package app.synco.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import app.synco.logging.SyncoLog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class FocusGate(
    private val service: AccessibilityService,
    private val focusTimeoutMillis: Long = DEFAULT_FOCUS_TIMEOUT_MILLIS,
) {
    private val gate = Mutex()

    suspend fun <T> withFocus(read: suspend () -> T): T? {
        if (!gate.tryLock()) {
            SyncoLog.clipboard.warn("skipped a copy because the previous capture was still running")
            return null
        }
        try {
            val windowManager = service.getSystemService(WindowManager::class.java) ?: return null
            val probe = FocusProbeView(service)
            val added = withContext(Dispatchers.Main) {
                runCatching { windowManager.addView(probe, focusableParams()) }
                    .onFailure { SyncoLog.clipboard.warn("could not add the focus overlay", it) }
                    .isSuccess
            }
            if (!added) return null
            try {
                val focused = withTimeoutOrNull(focusTimeoutMillis) { probe.awaitFocus() } != null
                if (!focused) {
                    SyncoLog.clipboard.warn(
                        "the focus overlay never gained focus within ${focusTimeoutMillis}ms",
                    )
                    return null
                }
                return read()
            } finally {
                withContext(NonCancellable + Dispatchers.Main) {
                    runCatching { windowManager.removeView(probe) }
                }
            }
        } finally {
            gate.unlock()
        }
    }

    private fun focusableParams(): WindowManager.LayoutParams =
        WindowManager.LayoutParams(
            OVERLAY_SIZE,
            OVERLAY_SIZE,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSPARENT,
        ).apply { gravity = Gravity.TOP or Gravity.START }

    private class FocusProbeView(context: Context) : View(context) {

        private val focused = CompletableDeferred<Unit>()

        suspend fun awaitFocus() = focused.await()

        override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
            super.onWindowFocusChanged(hasWindowFocus)
            if (hasWindowFocus && !focused.isCompleted) focused.complete(Unit)
        }

        override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean = false
    }

    private companion object {
        const val DEFAULT_FOCUS_TIMEOUT_MILLIS = 800L
        const val OVERLAY_SIZE = 1
    }
}
