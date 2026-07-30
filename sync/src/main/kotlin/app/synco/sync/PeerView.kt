package app.synco.sync

import app.synco.protocol.DeviceId
import app.synco.protocol.Fingerprint
import app.synco.protocol.Platform
import app.synco.protocol.message.Caps
import app.synco.protocol.message.CloseReason
import app.synco.storage.SyncPolicy

data class PeerView(
    val deviceId: DeviceId,
    val displayName: String,
    val platform: Platform?,
    val fingerprint: Fingerprint?,
    val status: PeerConnectionStatus,
    val policy: SyncPolicy,
    val trusted: Boolean,
    val rejected: Boolean,
    val discovered: Boolean,
    val dials: Boolean,
    val peerCaps: Caps?,
    val lastCloseReason: CloseReason?,
) {
    val isConnected: Boolean get() = status.isLive

    val isPairable: Boolean get() = discovered && !trusted && !rejected
}
