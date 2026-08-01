package app.synco.storage

enum class FocusTimeoutChoice(val millis: Long) {
    QUICK(250L),
    SWIFT(400L),
    STANDARD(800L),
    GENEROUS(1_200L),
    PATIENT(1_500L),
    VERY_PATIENT(2_500L),
    ;

    companion object {
        val DEFAULT = STANDARD

        fun nearest(millis: Long): FocusTimeoutChoice = entries.minBy { distance(it.millis, millis) }

        private fun distance(option: Long, millis: Long): Long =
            if (option > millis) option - millis else millis - option
    }
}
