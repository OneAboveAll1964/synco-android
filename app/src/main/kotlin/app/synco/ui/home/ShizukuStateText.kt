package app.synco.ui.home

import androidx.annotation.StringRes
import app.synco.R
import app.synco.shizuku.ShizukuState

@StringRes
internal fun shizukuStateTextRes(state: ShizukuState): Int = when (state) {
    ShizukuState.NOT_INSTALLED -> R.string.shizuku_not_installed
    ShizukuState.NOT_RUNNING -> R.string.shizuku_not_running
    ShizukuState.PERMISSION_DENIED -> R.string.shizuku_permission_denied
    ShizukuState.READY -> R.string.shizuku_ready
}
