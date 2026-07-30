package app.synco.sync

import app.synco.protocol.DeviceId

class ClipRouterFactory(
    private val clipboard: ClipboardSink,
    private val transfers: TransferGateway,
    private val blobs: BlobSender,
    private val events: SyncEventSink,
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
        )
}
