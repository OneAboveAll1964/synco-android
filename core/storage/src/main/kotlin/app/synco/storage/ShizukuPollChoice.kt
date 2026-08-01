package app.synco.storage

enum class ShizukuPollChoice(val millis: Long) {
    INSTANT(250L),
    RAPID(500L),
    BRISK(750L),
    STEADY(1_000L),
    RELAXED(2_000L),
    SPARING(4_000L),
    ;

    companion object {
        val DEFAULT = STEADY

        fun nearest(millis: Long): ShizukuPollChoice = entries.minBy { distance(it.millis, millis) }

        private fun distance(option: Long, millis: Long): Long =
            if (option > millis) option - millis else millis - option
    }
}
