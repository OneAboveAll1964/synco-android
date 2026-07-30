package app.synco.sync

import app.synco.protocol.DeviceId

data class SyncEvent(
    val kind: Kind,
    val peerDeviceId: DeviceId?,
    val detail: String?,
    val atMillis: Long,
) {
    enum class Kind {
        CLIP_SENT,
        CLIP_DROPPED,
        CLIP_ACKNOWLEDGED,
        CLIP_REFUSED,
        CLIP_APPLIED,
        CLIP_DECLINED,
        TRANSFER_FAILED,
        PEER_CONNECTED,
        PEER_DISCONNECTED,
        PEER_PAIRED,
        PEER_REJECTED,
        PAIRING_REQUESTED,
        ENGINE_FAILED,
    }

    companion object {
        fun of(
            kind: Kind,
            peerDeviceId: DeviceId? = null,
            detail: String? = null,
            atMillis: Long = System.currentTimeMillis(),
        ): SyncEvent = SyncEvent(kind, peerDeviceId, detail, atMillis)
    }
}
