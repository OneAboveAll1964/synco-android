package app.synco.ui.home

import androidx.annotation.StringRes
import app.synco.R
import app.synco.clipboard.ClipboardCaptureStatus

@StringRes
internal fun clipboardStatusTextRes(status: ClipboardCaptureStatus): Int = when (status) {
    ClipboardCaptureStatus.SERVICE_DISABLED -> R.string.clipboard_status_service_disabled
    ClipboardCaptureStatus.AWAITING_FIRST_COPY -> R.string.clipboard_status_awaiting_copy
    ClipboardCaptureStatus.WORKING -> R.string.clipboard_status_working
}
