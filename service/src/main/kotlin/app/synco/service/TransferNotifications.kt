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

    private val lastShownAt = HashMap<Int, Long>()

    fun createChannel() {
        manager?.deleteNotificationChannel(LEGACY_CHANNEL_ID)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.transfer_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.transfer_channel_description)
            setShowBadge(false)
            enableVibration(false)
            enableLights(false)
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
        val now = System.currentTimeMillis()
        val fresh = id !in shown
        if (!fresh && now - (lastShownAt[id] ?: 0L) < MIN_UPDATE_MILLIS) return
        shown += id
        lastShownAt[id] = now
        manager?.notify(id, build(progress))
    }

    fun dismissAll() {
        shown.toList().forEach(::dismiss)
    }

    fun retainOnly(liveIds: Set<java.util.UUID>) {
        val live = liveIds.map { it.hashCode() }.toSet()
        shown.toList().filterNot { it in live }.forEach(::dismiss)
    }

    private fun dismiss(id: Int) {
        lastShownAt.remove(id)
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
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setDefaults(0)
            .setLocalOnly(true)
            .setGroup(GROUP_KEY)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        if (LiveUpdateSupport.isAvailable(manager)) {
            TransferLiveUpdate.apply(context, builder, progress, percent)
        }
        return builder.build()
    }

    private companion object {
        const val CHANNEL_ID = "synco_transfers_pill"
        const val LEGACY_CHANNEL_ID = "synco_transfers"
        const val GROUP_KEY = "synco_transfers"
        const val PERCENT_TOTAL = 100
        const val MIN_UPDATE_MILLIS = 1_000L
    }
}
