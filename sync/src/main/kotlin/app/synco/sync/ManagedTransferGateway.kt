package app.synco.sync

import app.synco.protocol.framing.BlobChunk
import app.synco.protocol.message.TransferStart
import app.synco.transfer.OutgoingTransfer
import app.synco.transfer.TransferFailure
import app.synco.transfer.TransferManager
import app.synco.transfer.TransferOutcome
import app.synco.transfer.TransferProgress
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ManagedTransferGateway(private val transfers: TransferManager) : TransferGateway {

    override val progress: Flow<TransferProgress> get() = transfers.progress

    override fun beginIncoming(start: TransferStart, relativePath: String?): TransferFailure? =
        transfers.begin(start, relativePath)

    override fun acceptChunk(chunk: BlobChunk): TransferFailure? = transfers.accept(chunk)

    override fun completeIncoming(transferId: UUID): TransferOutcome = transfers.complete(transferId)

    override fun abortIncoming(transferId: UUID, failure: TransferFailure) {
        transfers.abortIncoming(transferId, failure)
    }

    override fun chunksOf(transfer: OutgoingTransfer): Flow<BlobChunk> = transfers.stream(transfer)

    override fun reportPeerProgress(transferId: UUID, receivedBytes: Long) {
        transfers.reportPeerProgress(transferId, receivedBytes)
    }

    override fun releaseOutgoing(transferId: UUID) {
        transfers.finishOutgoing(transferId)
    }

    override fun cancel(transferId: UUID) {
        transfers.abortIncoming(transferId, TransferFailure.CANCELLED)
        transfers.abortOutgoing(transferId)
    }

    override fun shutdown() {
        transfers.shutdown()
    }
}
