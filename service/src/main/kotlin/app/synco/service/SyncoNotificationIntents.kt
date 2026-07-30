package app.synco.service

import android.app.PendingIntent
import android.content.Context

internal class SyncoNotificationIntents(private val context: Context) {

    fun openApp(): PendingIntent? {
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: return null
        return PendingIntent.getActivity(context, REQUEST_OPEN, launch, FLAGS)
    }

    fun stop(): PendingIntent = PendingIntent.getForegroundService(
        context,
        REQUEST_STOP,
        SyncoServiceActions.stop(context),
        FLAGS,
    )

    fun pause(paused: Boolean): PendingIntent = PendingIntent.getForegroundService(
        context,
        if (paused) REQUEST_PAUSE else REQUEST_RESUME,
        SyncoServiceActions.pause(context, paused),
        FLAGS,
    )

    private companion object {
        const val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        const val REQUEST_OPEN = 1
        const val REQUEST_STOP = 2
        const val REQUEST_PAUSE = 3
        const val REQUEST_RESUME = 4
    }
}
