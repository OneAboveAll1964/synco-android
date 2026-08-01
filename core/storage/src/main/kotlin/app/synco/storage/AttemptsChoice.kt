package app.synco.storage

enum class AttemptsChoice(val attempts: Int) {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    SIX(6),
    EIGHT(8),
    ;

    companion object {
        val DEFAULT = TWO

        fun nearest(attempts: Int): AttemptsChoice = entries.minBy { distance(it.attempts, attempts) }

        private fun distance(option: Int, attempts: Int): Int =
            if (option > attempts) option - attempts else attempts - option
    }
}
