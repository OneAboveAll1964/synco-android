package app.synco.storage

data class CaptureTuning(
    val waitMillis: Long,
    val focusTimeoutMillis: Long,
) {
    companion object {
        val DEFAULT = CaptureTuning(
            waitMillis = CaptureWaitChoice.DEFAULT.millis,
            focusTimeoutMillis = FocusTimeoutChoice.DEFAULT.millis,
        )
    }
}
