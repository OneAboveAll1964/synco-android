package app.synco.sync

import app.synco.transfer.TransferProgress
import java.util.UUID

data class TransferView(
    val transferId: UUID,
    val name: String,
    val direction: TransferProgress.Direction,
    val state: TransferProgress.State,
    val bytesTransferred: Long,
    val totalBytes: Long,
) {
    val fraction: Float
        get() = if (totalBytes > 0) (bytesTransferred.toDouble() / totalBytes).toFloat() else 0f

    val isFinished: Boolean
        get() = state == TransferProgress.State.COMPLETED || state == TransferProgress.State.FAILED

    companion object {
        fun of(progress: TransferProgress): TransferView = TransferView(
            transferId = progress.transferId,
            name = progress.name,
            direction = progress.direction,
            state = progress.state,
            bytesTransferred = progress.bytesTransferred,
            totalBytes = progress.totalBytes,
        )
    }
}
