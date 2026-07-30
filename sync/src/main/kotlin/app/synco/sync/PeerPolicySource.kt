package app.synco.sync

import app.synco.storage.SyncPolicy

interface PeerPolicySource {
    val policy: SyncPolicy

    val peerMaxBlobBytes: Long
}
