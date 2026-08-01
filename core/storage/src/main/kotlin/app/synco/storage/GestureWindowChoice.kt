package app.synco.storage

enum class GestureWindowChoice(val millis: Long) {
    BRIEF(2_000L),
    SHORT(3_000L),
    MEDIUM(5_000L),
    RELAXED(8_000L),
    LONG(12_000L),
    VERY_LONG(20_000L),
    ;

    companion object {
        val DEFAULT = RELAXED

        fun nearest(millis: Long): GestureWindowChoice = entries.minBy { distance(it.millis, millis) }

        private fun distance(option: Long, millis: Long): Long =
            if (option > millis) option - millis else millis - option
    }
}
