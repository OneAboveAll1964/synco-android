package app.synco.sync

import app.synco.protocol.message.AckReason
import app.synco.protocol.message.ClipRep
import app.synco.storage.SyncPolicy

internal object ClipRejection {

    fun reasonFor(policy: SyncPolicy, reps: List<ClipRep>): AckReason {
        if (policy.paused) return AckReason.RECEIVE_DISABLED
        val reasons = reps.mapNotNull { policy.rejectionFor(it) }
        return when {
            reasons.isEmpty() -> AckReason.TYPE_DISABLED
            reasons.contains(AckReason.TYPE_DISABLED) -> AckReason.TYPE_DISABLED
            else -> reasons.first()
        }
    }
}
