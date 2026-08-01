package app.synco.sync

import app.synco.protocol.message.Ack
import app.synco.protocol.message.AckReason
import app.synco.protocol.message.TransferProgressReport
import app.synco.transfer.TransferFailure
import java.util.UUID

internal class ClipAcknowledger(
    private val link: PeerLink,
    private val clipboard: ClipboardSink,
    private val transfers: TransferGateway,
    private val events: SyncEventSink,
    private val files: InboundFilePublisher,
) {
    suspend fun apply(assembly: InboundClipAssembly) {
        val written = clipboard.apply(assembly.clip.hash, assembly.reps, assembly.blobs)
        if (!written) {
            decline(assembly.clip.id, AckReason.USER_CANCELLED)
            return
        }
        files.publish(assembly)
        link.send(Ack.applied(assembly.clip.id))
        events.record(SyncEvent.of(SyncEvent.Kind.CLIP_APPLIED, link.peerDeviceId, ClipSummary.of(assembly.clip.reps)))
    }

    suspend fun fail(assembly: InboundClipAssembly, failure: TransferFailure) {
        assembly.remaining.forEach { transferId -> abort(transferId, failure) }
        events.record(SyncEvent.of(SyncEvent.Kind.TRANSFER_FAILED, link.peerDeviceId, failure.wireValue))
        decline(assembly.clip.id, failure.ackReason ?: AckReason.HASH_MISMATCH)
    }

    suspend fun decline(clipId: String, reason: AckReason) {
        link.send(Ack.rejected(clipId, reason))
        events.record(SyncEvent.of(SyncEvent.Kind.CLIP_DECLINED, link.peerDeviceId, reason.wireValue))
    }

    suspend fun reportProgress(transferId: UUID, receivedBytes: Long) {
        quietly { link.send(TransferProgressReport(transferId.toString(), receivedBytes)) }
    }

    suspend fun abort(transferId: UUID, failure: TransferFailure) {
        transfers.abortIncoming(transferId, failure)
        quietly { link.send(failure.toAbort(transferId)) }
    }

    fun discard(assembly: InboundClipAssembly) {
        assembly.remaining.forEach { transfers.abortIncoming(it, TransferFailure.SHUTDOWN) }
    }
}
