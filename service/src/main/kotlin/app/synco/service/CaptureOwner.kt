package app.synco.service

import app.synco.shizuku.ShizukuState
import app.synco.storage.CaptureMode

internal object CaptureOwner {

    fun shizukuHasIt(mode: CaptureMode?, state: ShizukuState): Boolean =
        mode == CaptureMode.SHIZUKU && state.isUsable
}
