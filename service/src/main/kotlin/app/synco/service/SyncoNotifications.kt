package app.synco.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import app.synco.sync.SyncState

class SyncoNotifications(private val context: Context) {

    private val manager = context.getSystemService(NotificationManager::class.java)

    private val intents = SyncoNotificationIntents(context)

    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.synco_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.synco_notification_channel_description)
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
            setSound(null, null)
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }
        manager?.createNotificationChannel(channel)
    }

    fun build(state: SyncState): Notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_synco_notification)
        .setContentTitle(context.getString(R.string.synco_notification_title))
        .setContentText(SyncoStatusText.of(context, state))
        .setContentIntent(intents.openApp())
        .setOngoing(true)
        .setSilent(true)
        .setShowWhen(false)
        .setLocalOnly(true)
        .setOnlyAlertOnce(true)
        .setCategory(NotificationCompat.CATEGORY_SERVICE)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        .addAction(pauseAction(state.paused))
        .addAction(stopAction())
        .build()

    fun update(state: SyncState) {
        manager?.notify(NOTIFICATION_ID, build(state))
    }

    private fun pauseAction(paused: Boolean): NotificationCompat.Action {
        val icon = if (paused) R.drawable.ic_synco_resume else R.drawable.ic_synco_pause
        val label = if (paused) {
            R.string.synco_notification_action_resume
        } else {
            R.string.synco_notification_action_pause
        }
        return NotificationCompat.Action.Builder(
            icon,
            context.getString(label),
            intents.pause(!paused),
        ).build()
    }

    private fun stopAction(): NotificationCompat.Action = NotificationCompat.Action.Builder(
        R.drawable.ic_synco_stop,
        context.getString(R.string.synco_notification_action_stop),
        intents.stop(),
    ).build()

    companion object {
        const val CHANNEL_ID = "synco_clipboard_sync"
        const val NOTIFICATION_ID = 1001
    }
}
