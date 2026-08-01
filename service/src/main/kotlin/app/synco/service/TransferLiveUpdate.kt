package app.synco.service

import android.content.Context
import androidx.core.app.NotificationCompat
import app.synco.transfer.TransferProgress

internal object TransferLiveUpdate {

    fun apply(
        context: Context,
        builder: NotificationCompat.Builder,
        progress: TransferProgress,
        percent: Int,
    ): NotificationCompat.Builder = builder
        .setStyle(styleFor(progress, percent))
        .setShortCriticalText(context.getString(R.string.transfer_percent, percent))
        .setSubText(context.getString(R.string.transfer_app_name))
        .setColor(context.getColor(R.color.transfer_progress))
        .setColorized(false)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setRequestPromotedOngoing(true)
        .setUsesChronometer(false)
        .setShowWhen(false)

    private fun styleFor(
        progress: TransferProgress,
        percent: Int,
    ): NotificationCompat.ProgressStyle = if (progress.totalBytes <= 0) {
        NotificationCompat.ProgressStyle()
            .setProgressIndeterminate(true)
            .setStyledByProgress(true)
    } else {
        NotificationCompat.ProgressStyle()
            .setProgress(percent)
            .setStyledByProgress(true)
    }
}
