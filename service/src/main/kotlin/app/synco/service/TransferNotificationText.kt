package app.synco.service

import android.content.Context
import app.synco.transfer.TransferProgress

internal object TransferNotificationText {

    fun of(context: Context, progress: TransferProgress): String {
        val moved = ByteCount.of(progress.bytesTransferred)
        val total = ByteCount.of(progress.totalBytes)
        val resource = when (progress.direction) {
            TransferProgress.Direction.INCOMING -> R.string.transfer_receiving
            TransferProgress.Direction.OUTGOING -> R.string.transfer_sending
        }
        return context.getString(resource, moved, total)
    }
}
