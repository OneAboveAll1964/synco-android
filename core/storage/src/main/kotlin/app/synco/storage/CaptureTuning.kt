package app.synco.storage

data class CaptureTuning(
    val waitMillis: Long,
    val focusTimeoutMillis: Long,
    val attemptsPerGesture: Int,
    val gestureWindowMillis: Long,
) {
    companion object {
        val DEFAULT = CaptureTuning(
            waitMillis = CaptureWaitChoice.DEFAULT.millis,
            focusTimeoutMillis = FocusTimeoutChoice.DEFAULT.millis,
            attemptsPerGesture = AttemptsChoice.DEFAULT.attempts,
            gestureWindowMillis = GestureWindowChoice.DEFAULT.millis,
        )
    }
}
