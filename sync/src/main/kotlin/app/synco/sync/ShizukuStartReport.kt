package app.synco.sync

data class ShizukuStartReport(
    val started: Boolean,
    val reason: String?,
    val atMillis: Long = System.currentTimeMillis(),
)
