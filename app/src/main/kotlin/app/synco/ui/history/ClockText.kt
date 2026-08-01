package app.synco.ui.history

import java.text.DateFormat
import java.util.Date

object ClockText {

    private val format: DateFormat = DateFormat.getTimeInstance(DateFormat.SHORT)

    fun of(atMillis: Long): String = format.format(Date(atMillis))
}
