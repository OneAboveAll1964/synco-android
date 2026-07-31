package app.synco.storage

enum class CaptureWaitChoice(val millis: Long) {
    BRISK(120L),
    BALANCED(200L),
    PATIENT(400L),
    VERY_PATIENT(800L),
    ;

    companion object {
        val DEFAULT = BALANCED

        fun nearest(millis: Long): CaptureWaitChoice = entries.minBy { distance(it.millis, millis) }

        private fun distance(option: Long, millis: Long): Long =
            if (option > millis) option - millis else millis - option
    }
}
