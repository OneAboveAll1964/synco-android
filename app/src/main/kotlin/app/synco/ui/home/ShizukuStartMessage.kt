package app.synco.ui.home

import androidx.annotation.StringRes
import app.synco.R
import app.synco.sync.ShizukuStartReport

@StringRes
fun shizukuStartMessageRes(report: ShizukuStartReport): Int = when {
    report.started -> R.string.shizuku_started
    report.reason == NOT_ALLOWED -> R.string.shizuku_start_not_allowed
    report.reason == ADB_MISSING -> R.string.shizuku_start_adb_missing
    report.reason == NO_DEVICE -> R.string.shizuku_start_no_device
    report.reason == NOT_INSTALLED -> R.string.shizuku_start_not_installed
    report.reason == NO_STARTER -> R.string.shizuku_start_no_starter
    else -> R.string.shizuku_start_failed
}

private const val NOT_ALLOWED = "notAllowed"
private const val ADB_MISSING = "adbMissing"
private const val NO_DEVICE = "noDevice"
private const val NOT_INSTALLED = "notInstalled"
private const val NO_STARTER = "noStarter"
