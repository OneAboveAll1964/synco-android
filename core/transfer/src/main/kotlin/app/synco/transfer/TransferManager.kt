package app.synco.transfer

import android.content.ContentResolver
import app.synco.logging.SyncoLog
import app.synco.protocol.ProtocolConstants
import app.synco.protocol.framing.BlobChunk
import app.synco.protocol.message.TransferStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformWhile
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class TransferManager(
    private val storage: TransferStorage,
    private val resolver: ContentResolver,
    val maxBlobBytes: Long = ProtocolConstants.DEFAULT_MAX_BLOB_BYTES,
) {
    private val stager = BlobStager(resolver, storage)

    private val incoming = ConcurrentHashMap<UUID, IncomingTransfer>()
    private val outgoing = ConcurrentHashMap<UUID, OutgoingTransfer>()
    private val unfinished = ConcurrentHashMap.newKeySet<UUID>()
    private val reporter = TransferProgressReporter()

    val progress: SharedFlow<TransferProgress> get() = reporter.progress

    fun begin(start: TransferStart, relativePath: String? = null): TransferFailure? {
        val transferId = TransferIds.parseOrNull(start.transferId) ?: return TransferFailure.UNKNOWN_TRANSFER
        if (start.size < 0 || start.size > maxBlobBytes) return TransferFailure.TOO_LARGE
        val transfer = IncomingTransfer(transferId, start, storage, relativePath)
        incoming.put(transferId, transfer)?.fail(TransferFailure.CLOSED)
        reporter.incoming(transfer, TransferProgress.State.STARTED)
        return null
    }

    fun accept(chunk: BlobChunk): TransferFailure? {
        val transfer = incoming[chunk.transferId] ?: return TransferFailure.UNKNOWN_TRANSFER
        val failure = transfer.accept(chunk)
        if (failure != null) {
            abortIncoming(chunk.transferId, failure)
            return failure
        }
        reporter.incoming(transfer, TransferProgress.State.RUNNING)
        return null
    }

    fun complete(transferId: UUID): TransferOutcome {
        val transfer = incoming.remove(transferId)
            ?: return TransferOutcome.Failed(transferId, TransferFailure.UNKNOWN_TRANSFER)
        val outcome = transfer.complete()
        reporter.incoming(transfer, stateOf(outcome))
        return outcome
    }

    fun abortIncoming(transferId: UUID, failure: TransferFailure): TransferOutcome.Failed? {
        val transfer = incoming.remove(transferId) ?: return null
        val outcome = transfer.fail(failure)
        reporter.incoming(transfer, TransferProgress.State.FAILED)
        return outcome
    }

    fun findIncoming(transferId: UUID): IncomingTransfer? = incoming[transferId]

    suspend fun stageOutgoing(
        clipId: String,
        source: TransferSource,
        peerMaxBlobBytes: Long = maxBlobBytes,
        transferId: UUID = TransferIds.newId(),
    ): OutgoingTransfer? {
        val cap = minOf(peerMaxBlobBytes, maxBlobBytes)
        val staged = stager.stage(transferId, source, cap) ?: return null
        val prepared = OutgoingTransfer.fromStaged(
            resolver = resolver,
            clipId = clipId,
            staged = staged,
            name = source.name,
            mime = source.mime,
            transferId = transferId,
        )
        outgoing[transferId] = prepared
        return prepared
    }

    suspend fun prepareOutgoing(
        clipId: String,
        source: TransferSource,
        peerMaxBlobBytes: Long = maxBlobBytes,
        transferId: UUID = TransferIds.newId(),
    ): OutgoingTransfer? {
        val prepared = runCatching { OutgoingTransfer.prepare(resolver, clipId, source, transferId) }
            .onFailure { SyncoLog.transfer.warn("could not prepare outgoing blob", it) }
            .getOrNull() ?: return null
        if (prepared.size > peerMaxBlobBytes) {
            SyncoLog.transfer.warn(
                "outgoing blob refused, ${prepared.size} bytes exceeds the peer cap of $peerMaxBlobBytes",
            )
            release(transferId)
            return null
        }
        outgoing[transferId] = prepared
        return prepared
    }

    fun reportPeerProgress(transferId: UUID, receivedBytes: Long) {
        val transfer = outgoing[transferId] ?: return
        reporter.outgoing(transfer, TransferProgress.State.RUNNING, receivedBytes)
    }

    fun stream(transfer: OutgoingTransfer): Flow<BlobChunk> = transfer.chunks()
        .onStart {
            unfinished += transfer.transferId
            reporter.outgoing(transfer, TransferProgress.State.STARTED, 0L)
        }
        .transformWhile { chunk ->
            val live = isOutgoingLive(transfer.transferId)
            if (live) {
                emit(chunk)
                reporter.outgoing(transfer, TransferProgress.State.RUNNING, chunk.offset + chunk.data.size)
            }
            live
        }
        .onCompletion { error ->
            val broken = error != null || !isOutgoingLive(transfer.transferId)
            val state = if (broken) TransferProgress.State.FAILED else TransferProgress.State.COMPLETED
            unfinished -= transfer.transferId
            reporter.outgoing(transfer, state, transfer.size)
        }

    fun findOutgoing(transferId: UUID): OutgoingTransfer? = outgoing[transferId]

    fun isOutgoingLive(transferId: UUID): Boolean = outgoing.containsKey(transferId)

    fun finishOutgoing(transferId: UUID) = release(transferId)

    fun abortOutgoing(transferId: UUID) = release(transferId)

    fun isOutgoingUnfinished(transferId: UUID): Boolean = unfinished.contains(transferId)

    fun shutdown() {
        incoming.keys.toList().forEach { abortIncoming(it, TransferFailure.SHUTDOWN) }
        outgoing.keys.toList().forEach { release(it) }
        runCatching { storage.clearStaging() }
    }

    private fun release(transferId: UUID) {
        val transfer = outgoing.remove(transferId)
        if (transfer != null && unfinished.remove(transferId)) {
            SyncoLog.transfer.warn("outgoing ${transfer.name} was released before it finished")
            reporter.outgoing(transfer, TransferProgress.State.FAILED, 0L)
        } else {
            unfinished.remove(transferId)
        }
        runCatching { storage.stagingFile(transferId).delete() }
    }

    private fun stateOf(outcome: TransferOutcome): TransferProgress.State = when (outcome) {
        is TransferOutcome.Completed -> TransferProgress.State.COMPLETED
        is TransferOutcome.Failed -> TransferProgress.State.FAILED
    }
}
