package app.synco.transfer

import java.util.UUID

data class TransferProgress(
    val transferId: UUID,
    val direction: Direction,
    val state: State,
    val name: String,
    val bytesTransferred: Long,
    val totalBytes: Long,
) {
    val fraction: Float
        get() = if (totalBytes > 0) (bytesTransferred.toDouble() / totalBytes).toFloat() else 0f

    enum class Direction { INCOMING, OUTGOING }

    enum class State { STARTED, RUNNING, COMPLETED, FAILED }
}
