package app.synco.sync

import app.synco.protocol.message.Caps
import app.synco.storage.SyncPolicy

class PeerSettings(policy: SyncPolicy) : PeerPolicySource {

    @Volatile
    override var policy: SyncPolicy = policy

    @Volatile
    var peerCaps: Caps? = null

    override val peerMaxBlobBytes: Long
        get() = peerCaps?.maxBlobBytes ?: policy.maxBlobBytes
}
