package app.synco.sync

import app.synco.clipboard.ClipboardSnapshot
import app.synco.protocol.DeviceId
import app.synco.protocol.framing.BlobChunk
import app.synco.protocol.message.Ack
import app.synco.protocol.message.Clip
import app.synco.protocol.message.Envelope
import app.synco.protocol.message.TransferAbort
import app.synco.protocol.message.TransferEnd
import app.synco.protocol.message.TransferStart
import app.synco.transfer.ReceivedFileDestination
import app.synco.transfer.TransferIds

class ClipRouter(
    selfDeviceId: DeviceId,
    private val link: PeerLink,
    settings: PeerPolicySource,
    clipboard: ClipboardSink,
    transfers: TransferGateway,
    blobs: BlobSender,
    private val events: SyncEventSink,
    destination: ReceivedFileDestination,
    announcer: ReceivedFileAnnouncer,
) {
    private val aborts = OutboundAborts()

    private val files = InboundFilePublisher(destination, announcer)

    private val sender = ClipSender(selfDeviceId, link, settings, blobs, aborts, events)

    private val receiver = ClipReceiver(
        selfDeviceId = selfDeviceId,
        settings = settings,
        transfers = transfers,
        acknowledger = ClipAcknowledger(link, clipboard, transfers, events, files),
    )

    suspend fun send(snapshot: ClipboardSnapshot): Boolean = sender.send(snapshot)

    suspend fun receive(envelope: Envelope) {
        when (envelope) {
            is Clip -> receiver.onClip(envelope)
            is TransferStart -> receiver.onTransferStart(envelope)
            is TransferEnd -> receiver.onTransferEnd(envelope)
            is TransferAbort -> onAbort(envelope)
            is Ack -> onAck(envelope)
            else -> Unit
        }
    }

    suspend fun receive(chunk: BlobChunk) {
        receiver.onBlob(chunk)
    }

    fun cancel() {
        receiver.cancel()
    }

    private suspend fun onAbort(abort: TransferAbort) {
        TransferIds.parseOrNull(abort.transferId)?.let(aborts::record)
        receiver.onTransferAbort(abort)
    }

    private fun onAck(ack: Ack) {
        val kind = if (ack.applied) SyncEvent.Kind.CLIP_ACKNOWLEDGED else SyncEvent.Kind.CLIP_REFUSED
        events.record(SyncEvent.of(kind, link.peerDeviceId, ack.reason ?: ack.clipId))
    }
}
