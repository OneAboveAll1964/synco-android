package app.synco.sync

import app.synco.transfer.ReceivedFileDestination

import app.synco.clipboard.ClipboardSnapshot
import app.synco.protocol.DeviceId
import app.synco.protocol.ProtocolConstants
import app.synco.protocol.clip.ClipHash
import app.synco.protocol.message.CapsFlags
import app.synco.protocol.message.Clip
import app.synco.protocol.message.ClipRep
import app.synco.storage.PeerDirections
import app.synco.storage.SyncPolicy
import java.util.UUID

internal object ClipFixtures {

    val SELF = DeviceId("aaaaaaaaaaaaaaaa")
    val PEER = DeviceId("bbbbbbbbbbbbbbbb")

    const val CLIP_ID = "3f2a6d18-0c31-4d9a-9d55-1f2b3c4d5e6f"
    const val CAPTURED_AT_MILLIS = 1_730_300_000_000L

    fun policy(
        send: CapsFlags = CapsFlags.ALL_ENABLED,
        receive: CapsFlags = CapsFlags.ALL_ENABLED,
        paused: Boolean = false,
        maxBlobBytes: Long = ProtocolConstants.DEFAULT_MAX_BLOB_BYTES,
    ): SyncPolicy = SyncPolicy(PeerDirections(send, receive), paused, maxBlobBytes)

    fun snapshot(vararg reps: ClipRep): ClipboardSnapshot {
        val ordered = reps.toList()
        return ClipboardSnapshot(
            clipId = CLIP_ID,
            reps = ordered,
            hash = ClipHash.compute(ordered),
            transfers = emptyList(),
            capturedAtMillis = CAPTURED_AT_MILLIS,
        )
    }

    fun clip(vararg reps: ClipRep, origin: DeviceId = PEER, id: String = CLIP_ID): Clip {
        val ordered = reps.toList()
        return Clip(
            id = id,
            timestampMillis = CAPTURED_AT_MILLIS,
            origin = origin,
            hash = ClipHash.compute(ordered),
            reps = ordered,
        )
    }

    fun image(transferId: UUID, size: Long = 4L, name: String = "shot.png"): ClipRep.Image = ClipRep.Image(
        mime = "image/png",
        name = name,
        size = size,
        sha256 = "5f7b3c1d".repeat(8),
        transferId = transferId.toString(),
    )

    fun transferId(last: Int): UUID = UUID.fromString("00000000-0000-4000-8000-00000000000$last")

    fun router(
        link: PeerLink,
        settings: PeerPolicySource,
        clipboard: ClipboardSink = RecordingClipboard(),
        transfers: TransferGateway = StubTransferGateway(),
        blobs: BlobSender = StubBlobSender(),
        events: SyncEventSink = RecordingEvents(),
        destination: ReceivedFileDestination = NoReceivedFileDestination(),
        announcer: ReceivedFileAnnouncer = RecordingAnnouncer(),
    ): ClipRouter =
        ClipRouter(SELF, link, settings, clipboard, transfers, blobs, events, destination, announcer)
}
