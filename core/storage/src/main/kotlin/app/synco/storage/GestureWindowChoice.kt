package app.synco.storage

enum class GestureWindowChoice(val millis: Long) {
    SHORT(3_000L),
    MEDIUM(8_000L),
    LONG(15_000L),
    ;

    companion object {
        val DEFAULT = MEDIUM

        fun nearest(millis: Long): GestureWindowChoice = entries.minBy { distance(it.millis, millis) }

        private fun distance(option: Long, millis: Long): Long =
            if (option > millis) option - millis else millis - option
    }
}
