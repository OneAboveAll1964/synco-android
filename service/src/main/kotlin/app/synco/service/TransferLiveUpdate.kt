package app.synco.service

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import app.synco.transfer.TransferProgress

internal object TransferLiveUpdate {

    fun apply(
        context: Context,
        builder: NotificationCompat.Builder,
        progress: TransferProgress,
        percent: Int,
    ): NotificationCompat.Builder = builder
        .setStyle(styleFor(context, progress, percent))
        .setShortCriticalText(context.getString(R.string.transfer_percent, percent))
        .setRequestPromotedOngoing(true)
        .setUsesChronometer(false)
        .setShowWhen(false)

    private fun styleFor(
        context: Context,
        progress: TransferProgress,
        percent: Int,
    ): NotificationCompat.ProgressStyle = NotificationCompat.ProgressStyle()
        .setProgressSegments(listOf(NotificationCompat.ProgressStyle.Segment(SEGMENT_LENGTH)))
        .setProgress(percent)
        .setProgressIndeterminate(progress.totalBytes <= 0)
        .setStyledByProgress(true)
        .setProgressTrackerIcon(trackerIcon(context, progress))

    private fun trackerIcon(context: Context, progress: TransferProgress): IconCompat =
        IconCompat.createWithResource(
            context,
            when (progress.direction) {
                TransferProgress.Direction.INCOMING -> R.drawable.ic_transfer_incoming
                TransferProgress.Direction.OUTGOING -> R.drawable.ic_transfer_outgoing
            },
        )

    private const val SEGMENT_LENGTH = 100
}
