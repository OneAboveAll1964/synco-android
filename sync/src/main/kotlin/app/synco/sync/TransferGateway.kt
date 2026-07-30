package app.synco.sync

import app.synco.protocol.framing.BlobChunk
import app.synco.protocol.message.TransferStart
import app.synco.transfer.OutgoingTransfer
import app.synco.transfer.TransferFailure
import app.synco.transfer.TransferOutcome
import app.synco.transfer.TransferProgress
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface TransferGateway {

    val progress: Flow<TransferProgress>

    fun beginIncoming(start: TransferStart, relativePath: String?): TransferFailure?

    fun acceptChunk(chunk: BlobChunk): TransferFailure?

    fun completeIncoming(transferId: UUID): TransferOutcome

    fun abortIncoming(transferId: UUID, failure: TransferFailure)

    fun chunksOf(transfer: OutgoingTransfer): Flow<BlobChunk>

    fun releaseOutgoing(transferId: UUID)

    fun cancel(transferId: UUID)

    fun shutdown()
}
