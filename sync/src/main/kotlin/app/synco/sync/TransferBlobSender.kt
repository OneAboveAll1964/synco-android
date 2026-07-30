package app.synco.sync

import app.synco.clipboard.ClipboardSnapshot
import app.synco.protocol.message.ClipRep
import app.synco.protocol.message.TransferEnd
import app.synco.transfer.OutgoingTransfer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.takeWhile

class TransferBlobSender(private val transfers: TransferGateway) : BlobSender {

    override suspend fun stream(
        snapshot: ClipboardSnapshot,
        rep: ClipRep,
        link: PeerLink,
        aborts: OutboundAborts,
    ): Boolean {
        val transfer = transferFor(snapshot, rep) ?: return false
        link.send(transfer.start)
        val failure = streamFailure(transfer, link, aborts)
        val delivered = failure == null && !aborts.isAborted(transfer.transferId)
        quietly { link.send(TransferEnd(transfer.transferId.toString(), delivered)) }
        return delivered
    }

    private fun transferFor(snapshot: ClipboardSnapshot, rep: ClipRep): OutgoingTransfer? {
        val transferId = StreamedReps.transferIdOf(rep) ?: return null
        return snapshot.transfers.firstOrNull { it.transferId.toString() == transferId }
    }

    private suspend fun streamFailure(
        transfer: OutgoingTransfer,
        link: PeerLink,
        aborts: OutboundAborts,
    ): Throwable? = try {
        transfers.chunksOf(transfer)
            .takeWhile { !aborts.isAborted(transfer.transferId) }
            .collect { chunk -> link.send(chunk) }
        null
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Exception) {
        failure
    }
}
