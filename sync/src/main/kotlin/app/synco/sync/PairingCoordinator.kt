package app.synco.sync

import app.synco.logging.SyncoLog
import app.synco.protocol.DeviceId
import app.synco.storage.TrustedPeer
import app.synco.storage.TrustedPeerStore
import app.synco.transport.PairingApproval
import app.synco.transport.PairingResult
import app.synco.transport.PeerDescriptor
import kotlinx.coroutines.flow.StateFlow

class PairingCoordinator(
    private val trustedPeers: TrustedPeerStore,
    private val events: SyncEventSink,
    private val clock: () -> Long = System::currentTimeMillis,
) : PairingApproval {

    private val requests = PairingRequests()

    val pending: StateFlow<List<PendingPairing>> get() = requests.pending

    override suspend fun approve(peer: PeerDescriptor): Boolean {
        requests.remember(peer)
        val known = trustedPeers.find(peer.deviceId)
        if (known != null && known.rejected) {
            SyncoLog.session.info("refused ${peer.deviceId.value}, it was rejected before")
            return false
        }
        if (known != null && TrustedPeerRecords.holdsKeyOf(known, peer)) {
            SyncoLog.session.info("approved ${peer.deviceId.value} silently, its key is unchanged")
            return true
        }
        SyncoLog.session.info("asking about a pairing request from ${peer.deviceId.value}")
        events.record(
            SyncEvent.of(SyncEvent.Kind.PAIRING_REQUESTED, peer.deviceId, peer.fingerprint.grouped),
        )
        return requests.await(peer, clock())
    }

    fun decide(deviceId: DeviceId, approved: Boolean) {
        SyncoLog.session.info("the user ${if (approved) "approved" else "rejected"} ${deviceId.value}")
        requests.settle(deviceId, approved)
    }

    internal suspend fun settle(result: PairingResult): TrustedPeer? {
        val descriptor = requests.descriptorOf(result.peerDeviceId) ?: return null
        val existing = trustedPeers.find(result.peerDeviceId)
        val record = TrustedPeerRecords.of(
            peer = descriptor,
            pairedAtMillis = existing?.firstPairedAtMillis ?: clock(),
            rejected = result !is PairingResult.Approved,
        )
        trustedPeers.add(record)
        requests.forget(result.peerDeviceId)
        events.record(
            SyncEvent.of(
                kind = if (record.isTrusted) SyncEvent.Kind.PEER_PAIRED else SyncEvent.Kind.PEER_REJECTED,
                peerDeviceId = record.deviceId,
                detail = descriptor.fingerprint.grouped,
            ),
        )
        return record
    }
}
