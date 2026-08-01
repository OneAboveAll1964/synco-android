package app.synco.shizuku

import android.content.ClipData

sealed interface ShizukuRead {

    data class Clip(val data: ClipData?) : ShizukuRead

    data object Denied : ShizukuRead

    data object Unavailable : ShizukuRead
}
