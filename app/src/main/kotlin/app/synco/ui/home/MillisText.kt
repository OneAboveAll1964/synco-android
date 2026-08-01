package app.synco.ui.home

object MillisText {

    fun of(millis: Long): String =
        if (millis < SECOND) "$millis ms" else "${millis / SECOND}${fraction(millis)} s"

    private fun fraction(millis: Long): String {
        val tenths = millis % SECOND / 100
        return if (tenths == 0L) "" else ".$tenths"
    }

    private const val SECOND = 1000L
}
