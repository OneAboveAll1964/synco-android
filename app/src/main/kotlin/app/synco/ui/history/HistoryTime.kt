package app.synco.ui.history

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.synco.R
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

object ClockText {

    private val format: DateFormat = DateFormat.getTimeInstance(DateFormat.SHORT)

    fun of(atMillis: Long): String = format.format(Date(atMillis))
}

object DayText {

    private val format: DateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)

    fun of(context: Context, atMillis: Long, nowMillis: Long): String =
        when (daysApart(atMillis, nowMillis)) {
            0L -> context.getString(R.string.history_today)
            1L -> context.getString(R.string.history_yesterday)
            else -> format.format(Date(atMillis))
        }

    fun key(atMillis: Long): Long = startOfDay(atMillis)

    private fun daysApart(atMillis: Long, nowMillis: Long): Long {
        val difference = startOfDay(nowMillis) - startOfDay(atMillis)
        return difference / DAY_MILLIS
    }

    private fun startOfDay(atMillis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = atMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private const val DAY_MILLIS = 24L * 60 * 60 * 1000
}

@Composable
fun DayHeader(label: String, modifier: Modifier = Modifier) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
    )
}
