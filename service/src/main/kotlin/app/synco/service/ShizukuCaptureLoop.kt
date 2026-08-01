package app.synco.service

import app.synco.clipboard.CaptureRoute
import app.synco.clipboard.ClipboardCapture
import app.synco.logging.SyncoLog
import app.synco.shizuku.ShizukuAvailability
import app.synco.shizuku.ShizukuBinderWatch
import app.synco.shizuku.ShizukuClipboard
import app.synco.shizuku.ShizukuClipboardWatch
import app.synco.shizuku.ShizukuPermission
import app.synco.shizuku.ShizukuRead
import app.synco.shizuku.ShizukuState
import app.synco.storage.CaptureMode
import app.synco.sync.CaptureTuningHolder
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShizukuCaptureLoop(
    private val availability: ShizukuAvailability,
    private val clipboard: ShizukuClipboard,
    private val capture: ClipboardCapture,
    private val tuning: CaptureTuningHolder,
    private val watch: ShizukuClipboardWatch = ShizukuClipboardWatch(),
) {
    private val stateFlow = MutableStateFlow(ShizukuState.NOT_INSTALLED)

    val state: StateFlow<ShizukuState> = stateFlow.asStateFlow()

    private val nudges = Channel<Unit>(1, BufferOverflow.DROP_OLDEST)

    private var lastFingerprint: String? = null

    suspend fun run(): Unit = coroutineScope {
        ShizukuPermission.observe { refresh() }
        ShizukuBinderWatch.observe(::refresh)
        launch { captureOnNudge() }
        while (true) {
            val read = clipboard.read()
            publish(availability.state { read })
            delay(tick(read))
        }
    }

    private suspend fun tick(read: ShizukuRead): Long {
        if (!chosen() || !stateFlow.value.isUsable) {
            watch.stop()
            return if (chosen()) UNAVAILABLE_MILLIS else IDLE_MILLIS
        }
        val watching = watch.start { nudges.trySend(Unit) }
        consider(read)
        return if (watching) WATCHING_MILLIS else FALLBACK_POLL_MILLIS
    }

    private suspend fun captureOnNudge() {
        for (ignored in nudges) {
            if (!chosen() || !stateFlow.value.isUsable) continue
            consider(clipboard.read())
        }
    }

    fun stopWatching() = watch.stop()

    private fun chosen(): Boolean = tuning.mode() == CaptureMode.SHIZUKU

    private fun publish(next: ShizukuState) {
        if (next != stateFlow.value) SyncoLog.clipboard.info("Shizuku capture is $next")
        stateFlow.value = next
    }

    private fun refresh() {
        stateFlow.value = availability.state(clipboard::read)
    }

    private suspend fun consider(read: ShizukuRead) {
        val clip = (read as? ShizukuRead.Clip)?.data ?: return
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
        const val WATCHING_MILLIS = 15_000L
        const val FALLBACK_POLL_MILLIS = 700L
    }
}
