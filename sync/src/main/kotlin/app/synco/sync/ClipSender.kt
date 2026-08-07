package app.synco.sync

import app.synco.clipboard.ClipboardSnapshot
import app.synco.logging.SyncoLog
import app.synco.protocol.DeviceId
import app.synco.protocol.clip.ClipHash
import app.synco.protocol.message.Clip
import app.synco.protocol.message.ClipRep

internal class ClipSender(
    private val selfDeviceId: DeviceId,
    private val link: PeerLink,
    private val settings: PeerPolicySource,
    private val blobs: BlobSender,
    private val aborts: OutboundAborts,
    private val events: SyncEventSink,
) {
    suspend fun send(snapshot: ClipboardSnapshot): Boolean {
        val sendable = settings.policy.sendableReps(snapshot.reps, settings.peerMaxBlobBytes)
        if (sendable.isEmpty()) {
            events.record(SyncEvent.of(SyncEvent.Kind.CLIP_DROPPED, link.peerDeviceId, ClipSummary.of(snapshot.reps)))
            return false
        }
        val clip = clipOf(snapshot, sendable)
        link.send(clip)
        for (rep in sendable.filter { StreamedReps.transferIdOf(it) != null }) {
            if (blobs.stream(snapshot, rep, link, aborts)) continue
            events.record(SyncEvent.of(SyncEvent.Kind.TRANSFER_FAILED, link.peerDeviceId, ClipSummary.of(sendable)))
            return false
        }
        events.record(SyncEvent.of(SyncEvent.Kind.CLIP_SENT, link.peerDeviceId, ClipSummary.of(sendable)))
        return true
    }

    private fun clipOf(snapshot: ClipboardSnapshot, sendable: List<ClipRep>): Clip = Clip(
        id = snapshot.clipId,
        timestampMillis = snapshot.capturedAtMillis,
        origin = selfDeviceId,
        hash = if (sendable.size == snapshot.reps.size) snapshot.hash else ClipHash.compute(sendable),
        reps = sendable,
    )
}
