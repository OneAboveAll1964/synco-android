package app.synco.service

import app.synco.clipboard.CaptureRoute
import app.synco.clipboard.ClipboardCapture
import app.synco.logging.SyncoLog
import app.synco.shizuku.ShizukuAvailability
import app.synco.shizuku.ShizukuClipboard
import app.synco.shizuku.ShizukuBinderWatch
import app.synco.shizuku.ShizukuPermission
import app.synco.shizuku.ShizukuState
import app.synco.storage.CaptureMode
import app.synco.sync.CaptureTuningHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ShizukuCaptureLoop(
    private val availability: ShizukuAvailability,
    private val clipboard: ShizukuClipboard,
    private val capture: ClipboardCapture,
    private val tuning: CaptureTuningHolder,
) {
    private val stateFlow = MutableStateFlow(ShizukuState.NOT_INSTALLED)

    val state: StateFlow<ShizukuState> = stateFlow.asStateFlow()

    private var lastFingerprint: String? = null

    suspend fun run() {
        ShizukuPermission.observe { refresh() }
        ShizukuBinderWatch.observe(::refresh)
        while (true) {
            val current = availability.state(clipboard::read)
            if (current != stateFlow.value) {
                SyncoLog.clipboard.info("Shizuku capture is $current")
            }
            stateFlow.value = current
            val chosen = tuning.mode() == CaptureMode.SHIZUKU
            if (!chosen) {
                delay(IDLE_MILLIS)
                continue
            }
            if (current.isUsable) pollOnce()
            delay(if (current.isUsable) tuning.shizukuPollMillis() else UNAVAILABLE_MILLIS)
        }
    }

    private fun refresh() {
        stateFlow.value = availability.state(clipboard::read)
    }

    private suspend fun pollOnce() {
        val clip = clipboard.primaryClip()
        if (clip == null) {
            SyncoLog.clipboard.debug { "Shizuku read gave no clip" }
            return
        }
        val fingerprint = fingerprintOf(clip)
        if (fingerprint == lastFingerprint) return
        lastFingerprint = fingerprint
        SyncoLog.clipboard.debug { "Shizuku observed a new clip" }
        capture.captureClip(clip, CaptureRoute.SHIZUKU)
    }

    private fun fingerprintOf(clip: android.content.ClipData): String = buildString {
        append(clip.itemCount)
        for (index in 0 until clip.itemCount) {
            val item = clip.getItemAt(index)
            append('|').append(item.text ?: "").append('#').append(item.uri ?: "")
        }
    }

    private companion object {
        const val IDLE_MILLIS = 2_000L
        const val UNAVAILABLE_MILLIS = 3_000L
    }
}
