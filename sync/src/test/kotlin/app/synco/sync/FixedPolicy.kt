package app.synco.sync

import app.synco.protocol.ProtocolConstants
import app.synco.storage.SyncPolicy

internal class FixedPolicy(
    override var policy: SyncPolicy = ClipFixtures.policy(),
    override val peerMaxBlobBytes: Long = ProtocolConstants.DEFAULT_MAX_BLOB_BYTES,
) : PeerPolicySource
