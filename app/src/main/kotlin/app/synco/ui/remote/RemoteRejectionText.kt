package app.synco.ui.remote

import androidx.annotation.StringRes
import app.synco.R

@StringRes
fun remoteRejectionTextRes(reason: String): Int = when (reason) {
    "screenPermission" -> R.string.remote_reject_screen
    "inputPermission" -> R.string.remote_reject_input
    "busy" -> R.string.remote_reject_busy
    else -> R.string.remote_reject_unsupported
}
