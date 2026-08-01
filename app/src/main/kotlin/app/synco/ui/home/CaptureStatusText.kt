package app.synco.ui.home

import androidx.annotation.StringRes
import app.synco.R
import app.synco.clipboard.ClipboardCaptureStatus
import app.synco.shizuku.ShizukuState
import app.synco.storage.CaptureMode

@StringRes
internal fun captureStatusTextRes(
    mode: CaptureMode,
    shizuku: ShizukuState,
    accessibility: ClipboardCaptureStatus,
): Int = when {
    mode != CaptureMode.SHIZUKU -> clipboardStatusTextRes(accessibility)
    shizuku.isUsable -> R.string.clipboard_status_shizuku_working
    else -> clipboardStatusTextRes(accessibility)
}
