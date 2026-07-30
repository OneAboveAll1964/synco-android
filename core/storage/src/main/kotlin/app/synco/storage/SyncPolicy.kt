package app.synco.storage

import app.synco.protocol.ProtocolConstants
import app.synco.protocol.message.AckReason
import app.synco.protocol.message.Caps
import app.synco.protocol.message.CapsFlags
import app.synco.protocol.message.ClipRep

data class SyncPolicy(
    val directions: PeerDirections,
    val paused: Boolean,
    val maxBlobBytes: Long,
) {
    val send: CapsFlags get() = if (paused) CapsFlags.ALL_DISABLED else directions.send

    val receive: CapsFlags get() = if (paused) CapsFlags.ALL_DISABLED else directions.receive

    fun maySend(rep: ClipRep, peerMaxBlobBytes: Long = maxBlobBytes): Boolean =
        send.allows(ClipCategory.of(rep)) && blobSizeOf(rep) <= peerMaxBlobBytes

    fun sendableReps(reps: List<ClipRep>, peerMaxBlobBytes: Long = maxBlobBytes): List<ClipRep> =
        reps.filter { maySend(it, peerMaxBlobBytes) }

    fun mayAccept(rep: ClipRep): Boolean = rejectionFor(rep) == null

    fun acceptableReps(reps: List<ClipRep>): List<ClipRep> = reps.filter(::mayAccept)

    fun rejectionFor(rep: ClipRep): AckReason? = when {
        paused -> AckReason.RECEIVE_DISABLED
        !directions.receive.allows(ClipCategory.of(rep)) -> AckReason.TYPE_DISABLED
        !withinBlobBudget(blobSizeOf(rep)) -> AckReason.TOO_LARGE
        else -> null
    }

    fun withinBlobBudget(sizeBytes: Long): Boolean = sizeBytes in 0..maxBlobBytes

    fun toCaps(): Caps = Caps(accepts = receive, sends = send, maxBlobBytes = maxBlobBytes)

    private fun blobSizeOf(rep: ClipRep): Long = when (rep) {
        is ClipRep.Image -> rep.size
        is ClipRep.File -> rep.size
        else -> 0L
    }

    companion object {
        val DEFAULT = SyncPolicy(
            directions = PeerDirections.ALL_ENABLED,
            paused = false,
            maxBlobBytes = ProtocolConstants.DEFAULT_MAX_BLOB_BYTES,
        )
    }
}
