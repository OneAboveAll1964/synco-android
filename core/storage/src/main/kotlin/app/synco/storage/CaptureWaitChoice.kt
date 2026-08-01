package app.synco.storage

enum class CaptureWaitChoice(val millis: Long) {
    VERY_BRISK(80L),
    BRISK(120L),
    BALANCED(200L),
    RELAXED(250L),
    PATIENT(400L),
    VERY_PATIENT(600L),
    THOROUGH(800L),
    EXHAUSTIVE(1_200L),
    ;

    companion object {
        val DEFAULT = BALANCED

        fun nearest(millis: Long): CaptureWaitChoice = entries.minBy { distance(it.millis, millis) }

        private fun distance(option: Long, millis: Long): Long =
            if (option > millis) option - millis else millis - option
    }
}
