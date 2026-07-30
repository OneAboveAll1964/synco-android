package app.synco.transfer

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class TransferProgressReporter {

    private val events = MutableSharedFlow<TransferProgress>(
        extraBufferCapacity = PROGRESS_BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val progress: SharedFlow<TransferProgress> = events.asSharedFlow()

    fun incoming(transfer: IncomingTransfer, state: TransferProgress.State) {
        events.tryEmit(
            TransferProgress(
                transferId = transfer.transferId,
                direction = TransferProgress.Direction.INCOMING,
                state = state,
                name = transfer.name,
                bytesTransferred = transfer.bytesWritten,
                totalBytes = transfer.expectedSize,
            ),
        )
    }

    fun outgoing(transfer: OutgoingTransfer, state: TransferProgress.State, bytesTransferred: Long) {
        events.tryEmit(
            TransferProgress(
                transferId = transfer.transferId,
                direction = TransferProgress.Direction.OUTGOING,
                state = state,
                name = transfer.name,
                bytesTransferred = bytesTransferred,
                totalBytes = transfer.size,
            ),
        )
    }

    private companion object {
        const val PROGRESS_BUFFER = 64
    }
}
