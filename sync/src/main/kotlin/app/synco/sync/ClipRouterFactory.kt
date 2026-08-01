package app.synco.sync

import app.synco.protocol.DeviceId
import app.synco.transfer.ReceivedFileDestination

class ClipRouterFactory(
    private val clipboard: ClipboardSink,
    private val transfers: TransferGateway,
    private val blobs: BlobSender,
    private val events: SyncEventSink,
    private val destination: ReceivedFileDestination,
    private val announcer: ReceivedFileAnnouncer,
    private val shizuku: ShizukuStartSink = ShizukuStartSink.NONE,
) {
    fun create(selfDeviceId: DeviceId, link: PeerLink, settings: PeerPolicySource): ClipRouter =
        ClipRouter(
            selfDeviceId = selfDeviceId,
            link = link,
            settings = settings,
            clipboard = clipboard,
            transfers = transfers,
            blobs = blobs,
            events = events,
            destination = destination,
            announcer = announcer,
            shizuku = shizuku,
        )
}
