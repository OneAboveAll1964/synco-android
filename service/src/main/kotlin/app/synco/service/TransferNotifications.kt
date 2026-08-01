package app.synco.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import app.synco.transfer.TransferProgress

class TransferNotifications(private val context: Context) {

    private val manager = context.getSystemService(NotificationManager::class.java)

    private val intents = SyncoNotificationIntents(context)

    private val shown = mutableSetOf<Int>()

    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.transfer_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.transfer_channel_description)
            setShowBadge(false)
            enableVibration(false)
            setSound(null, null)
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }
        manager?.createNotificationChannel(channel)
    }

    fun publish(progress: TransferProgress) {
        val id = progress.transferId.hashCode()
        if (progress.isFinished) {
            dismiss(id)
            return
        }
        if (!progress.deservesNotification) return
        shown += id
        manager?.notify(id, build(progress))
    }

    fun dismissAll() {
        shown.toList().forEach(::dismiss)
    }

    private fun dismiss(id: Int) {
        if (!shown.remove(id)) return
        manager?.cancel(id)
    }

    private fun build(progress: TransferProgress): Notification {
        val percent = (progress.fraction * 100).toInt().coerceIn(0, 100)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_synco_notification)
            .setContentTitle(progress.name)
            .setContentText(TransferNotificationText.of(context, progress))
            .setContentIntent(intents.openApp())
            .setProgress(PERCENT_TOTAL, percent, progress.totalBytes <= 0)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setLocalOnly(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        return builder.build()
    }

    private companion object {
        const val CHANNEL_ID = "synco_transfers"
        const val PERCENT_TOTAL = 100
    }
}
