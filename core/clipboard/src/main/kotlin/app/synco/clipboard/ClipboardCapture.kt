package app.synco.clipboard

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ClipboardCapture {

    val changes: Flow<CapturedClip>

    val status: StateFlow<ClipboardCaptureStatus>

    suspend fun captureVia(route: CaptureRoute): Boolean

    fun clipTimestamp(): Long?

    fun setAccessibilityConnected(connected: Boolean)
}
