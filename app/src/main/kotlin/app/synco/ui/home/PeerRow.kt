package app.synco.ui.home

import app.synco.protocol.DeviceId
import app.synco.protocol.Fingerprint
import app.synco.protocol.Platform
import app.synco.protocol.message.CapsFlags
import app.synco.storage.SyncPolicy
import app.synco.storage.TrustedPeer
import app.synco.sync.PeerConnectionStatus
import app.synco.sync.PeerView
import app.synco.sync.SyncDirection

data class PeerRow(
    val deviceId: DeviceId,
    val displayName: String,
    val fingerprint: Fingerprint?,
    val platform: Platform?,
    val status: PeerConnectionStatus,
    val trusted: Boolean,
    val direction: SyncDirection,
    val send: CapsFlags,
    val receive: CapsFlags,
    val peerAccepts: CapsFlags?,
    val startsShizukuOverAdb: Boolean = false,
) {
    val isConnected: Boolean get() = status.isLive

    val isRejected: Boolean get() = status == PeerConnectionStatus.REJECTED

    val canControl: Boolean get() = trusted && isConnected && platform == Platform.MACOS

    companion object {
        fun of(view: PeerView): PeerRow = PeerRow(
            deviceId = view.deviceId,
            displayName = view.displayName,
            fingerprint = view.fingerprint,
            platform = view.platform,
            status = view.status,
            trusted = view.trusted,
            direction = SyncDirection.of(view.policy.directions),
            send = view.policy.directions.send,
            receive = view.policy.directions.receive,
            peerAccepts = view.peerCaps?.accepts,
            startsShizukuOverAdb = view.peerCaps?.adbShizuku == true,
        )

        fun of(peer: TrustedPeer, policy: SyncPolicy): PeerRow = PeerRow(
            deviceId = peer.deviceId,
            displayName = peer.displayName,
            fingerprint = peer.fingerprint,
            platform = null,
            status = if (peer.rejected) PeerConnectionStatus.REJECTED else PeerConnectionStatus.OFFLINE,
            trusted = peer.isTrusted,
            direction = SyncDirection.of(policy.directions),
            send = policy.directions.send,
            receive = policy.directions.receive,
            peerAccepts = null,
        )
    }
}
