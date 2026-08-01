package app.synco.sync

import app.synco.protocol.framing.BlobChunk
import app.synco.protocol.message.TransferStart
import app.synco.transfer.OutgoingTransfer
import app.synco.transfer.TransferFailure
import app.synco.transfer.TransferOutcome
import app.synco.transfer.TransferProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.io.File
import java.util.UUID

internal class StubTransferGateway : TransferGateway {

    val started = mutableListOf<TransferStart>()
    val accepted = mutableListOf<BlobChunk>()
    val aborted = mutableListOf<UUID>()
    val released = mutableListOf<UUID>()

    var beginFailure: TransferFailure? = null

    private val outcomes = mutableMapOf<UUID, TransferOutcome>()

    override val progress: Flow<TransferProgress> = emptyFlow()

    fun completeWith(transferId: UUID, outcome: TransferOutcome) {
        outcomes[transferId] = outcome
    }

    override fun beginIncoming(start: TransferStart, relativePath: String?): TransferFailure? {
        started += start
        return beginFailure
    }

    override fun acceptChunk(chunk: BlobChunk): TransferFailure? {
        accepted += chunk
        return null
    }

    override fun completeIncoming(transferId: UUID): TransferOutcome =
        outcomes[transferId] ?: TransferOutcome.Completed(
            transferId = transferId,
            file = File("$transferId.bin"),
            name = "$transferId.bin",
            mime = "application/octet-stream",
            size = 4L,
            sha256 = "5f7b3c1d".repeat(8),
        )

    override fun abortIncoming(transferId: UUID, failure: TransferFailure) {
        aborted += transferId
    }

    override fun chunksOf(transfer: OutgoingTransfer): Flow<BlobChunk> = emptyFlow()

    override fun reportPeerProgress(transferId: UUID, receivedBytes: Long) = Unit

    override fun releaseOutgoing(transferId: UUID) {
        released += transferId
    }

    override fun cancel(transferId: UUID) {
        aborted += transferId
    }

    override fun shutdown() = Unit
}
