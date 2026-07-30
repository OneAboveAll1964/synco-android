package app.synco.sync

import app.synco.protocol.DeviceId
import app.synco.protocol.framing.BlobChunk
import app.synco.protocol.message.AckReason
import app.synco.protocol.message.Clip
import app.synco.protocol.message.TransferAbort
import app.synco.protocol.message.TransferEnd
import app.synco.protocol.message.TransferStart
import app.synco.transfer.TransferFailure
import app.synco.transfer.TransferIds
import app.synco.transfer.TransferOutcome

internal class ClipReceiver(
    private val selfDeviceId: DeviceId,
    private val settings: PeerPolicySource,
    private val transfers: TransferGateway,
    private val acknowledger: ClipAcknowledger,
) {
    private val held = InboundClipStore()

    suspend fun onClip(clip: Clip) {
        if (clip.origin == selfDeviceId) return
        val policy = settings.policy
        val acceptable = policy.acceptableReps(clip.reps)
        if (acceptable.isEmpty()) {
            acknowledger.decline(clip.id, ClipRejection.reasonFor(policy, clip.reps))
            return
        }
        val usable = acceptable.filter(StreamedReps::isReconstructable)
        if (usable.isEmpty()) {
            acknowledger.decline(clip.id, AckReason.HASH_MISMATCH)
            return
        }
        val assembly = InboundClipAssembly(clip, usable)
        if (assembly.awaitsTransfers) held.hold(assembly) else acknowledger.apply(assembly)
    }

    suspend fun onTransferStart(start: TransferStart) {
        val transferId = TransferIds.parseOrNull(start.transferId) ?: return
        val assembly = held.forTransfer(transferId)
        if (assembly == null) {
            acknowledger.abort(transferId, TransferFailure.UNKNOWN_TRANSFER)
            return
        }
        if (assembly.clip.id != start.clipId) {
            fail(assembly, TransferFailure.UNKNOWN_TRANSFER)
            return
        }
        val failure = transfers.beginIncoming(start, assembly.relativePathOf(transferId))
        if (failure != null) fail(assembly, failure)
    }

    suspend fun onBlob(chunk: BlobChunk) {
        val assembly = held.forTransfer(chunk.transferId) ?: return
        val failure = transfers.acceptChunk(chunk)
        if (failure != null) fail(assembly, failure)
    }

    suspend fun onTransferEnd(end: TransferEnd) {
        val transferId = TransferIds.parseOrNull(end.transferId) ?: return
        val assembly = held.forTransfer(transferId) ?: return
        if (!end.ok) {
            fail(assembly, TransferFailure.INCOMPLETE)
            return
        }
        when (val outcome = transfers.completeIncoming(transferId)) {
            is TransferOutcome.Completed -> {
                assembly.record(transferId, outcome.file)
                if (!assembly.awaitsTransfers) {
                    held.release(assembly)
                    acknowledger.apply(assembly)
                }
            }
            is TransferOutcome.Failed -> fail(assembly, outcome.failure)
        }
    }

    suspend fun onTransferAbort(abort: TransferAbort) {
        val transferId = TransferIds.parseOrNull(abort.transferId) ?: return
        val assembly = held.forTransfer(transferId) ?: return
        fail(assembly, TransferFailure.fromWire(abort.reason) ?: TransferFailure.CANCELLED)
    }

    fun cancel() {
        held.drain().forEach(acknowledger::discard)
    }

    private suspend fun fail(assembly: InboundClipAssembly, failure: TransferFailure) {
        held.release(assembly)
        acknowledger.fail(assembly, failure)
    }
}
