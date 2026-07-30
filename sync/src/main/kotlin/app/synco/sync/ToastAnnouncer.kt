package app.synco.sync

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

class ToastAnnouncer(context: Context) : ReceivedFileAnnouncer {

    private val context: Context = context.applicationContext

    private val main = Handler(Looper.getMainLooper())

    override fun announce(arrival: ReceivedFileArrival) {
        val message = messageFor(arrival)
        main.post { Toast.makeText(context, message, Toast.LENGTH_LONG).show() }
    }

    private fun messageFor(arrival: ReceivedFileArrival): String {
        val subject = arrival.singleName ?: context.getString(R.string.received_files_count, arrival.count)
        val location = arrival.location
        return if (arrival.browsable && !location.isNullOrBlank()) {
            context.getString(R.string.received_saved_to, subject, location)
        } else {
            context.getString(R.string.received_saved, subject)
        }
    }
}
