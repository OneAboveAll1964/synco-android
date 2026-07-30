package app.synco.clipboard

import app.synco.protocol.DeviceId
import app.synco.protocol.message.Clip
import app.synco.protocol.message.ClipRep
import app.synco.transfer.OutgoingTransfer

class ClipboardSnapshot(
    val clipId: String,
    val reps: List<ClipRep>,
    val hash: String,
    val transfers: List<OutgoingTransfer>,
    val capturedAtMillis: Long,
) {
    fun toClip(origin: DeviceId): Clip = Clip(
        id = clipId,
        timestampMillis = capturedAtMillis,
        origin = origin,
        hash = hash,
        reps = reps,
    )
}
