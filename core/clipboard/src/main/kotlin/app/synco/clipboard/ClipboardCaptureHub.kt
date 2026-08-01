package app.synco.clipboard

import app.synco.logging.SyncoLog
import app.synco.protocol.ProtocolConstants
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class ClipboardCaptureHub(
    private val reader: ClipboardReader,
    private val suppression: SuppressionWindow,
    private val maxBlobBytes: () -> Long = { ProtocolConstants.DEFAULT_MAX_BLOB_BYTES },
    captureWaitMillis: () -> Long = { ClipSettle.DEFAULT_BUDGET_MILLIS },
) : ClipboardCapture {

    private val emissions = MutableSharedFlow<CapturedClip>(
        extraBufferCapacity = EMISSION_BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val readLock = Mutex()

    private val settle = ClipSettle(captureWaitMillis)

    private val connected = MutableStateFlow(false)

    private val observedAccessibilityCopy = MutableStateFlow(false)

    private val statusFlow = MutableStateFlow(ClipboardCaptureStatus.SERVICE_DISABLED)

    override val changes: Flow<CapturedClip> = emissions

    override val status: StateFlow<ClipboardCaptureStatus> = statusFlow.asStateFlow()

    override suspend fun captureVia(route: CaptureRoute): Boolean {
        val snapshot = readLock.withLock {
            val settled = settle.awaitFresh(reader::clipTimestamp)
            if (settled == SettleOutcome.STALE) {
                SyncoLog.clipboard.debug { "the clipboard never changed after a copy signal" }
                return false
            }
            val candidate = reader.read(UUID.randomUUID().toString(), maxBlobBytes()) ?: return false
            if (suppression.consume(candidate.hash)) {
                SyncoLog.clipboard.debug { "ignored a clip we had just written locally" }
                return false
            }
            candidate
        }
        SyncoLog.clipboard.info(
            "captured a clip via $route with ${snapshot.reps.size} representation(s) " +
                "and ${snapshot.transfers.size} blob(s)",
        )
        markObserved(route)
        emissions.emit(CapturedClip(snapshot, route))
        return true
    }

    override suspend fun captureClip(clip: android.content.ClipData, route: CaptureRoute): Boolean {
        val snapshot = readLock.withLock {
            val candidate = reader.readClip(clip, UUID.randomUUID().toString(), maxBlobBytes())
                ?: return false
            if (suppression.consume(candidate.hash)) {
                SyncoLog.clipboard.debug { "ignored a clip we had just written locally" }
                return false
            }
            candidate
        }
        SyncoLog.clipboard.info(
            "captured a clip via $route with ${snapshot.reps.size} representation(s) " +
                "and ${snapshot.transfers.size} blob(s)",
        )
        markObserved(route)
        emissions.emit(CapturedClip(snapshot, route))
        return true
    }

    override fun clipTimestamp(): Long? = reader.clipTimestamp()

    override fun setAccessibilityConnected(connected: Boolean) {
        SyncoLog.clipboard.info("accessibility capture route ${if (connected) "connected" else "disconnected"}")
        this.connected.value = connected
        recomputeStatus()
    }

    private fun markObserved(route: CaptureRoute) {
        if (route == CaptureRoute.FOREGROUND_LISTENER) return
        observedAccessibilityCopy.value = true
        recomputeStatus()
    }

    private fun recomputeStatus() {
        val next = when {
            !connected.value -> ClipboardCaptureStatus.SERVICE_DISABLED
            !observedAccessibilityCopy.value -> ClipboardCaptureStatus.AWAITING_FIRST_COPY
            else -> ClipboardCaptureStatus.WORKING
        }
        SyncoLog.clipboard.debug { "capture status is now $next" }
        statusFlow.value = next
    }

    private companion object {
        const val EMISSION_BUFFER = 8
    }
}
