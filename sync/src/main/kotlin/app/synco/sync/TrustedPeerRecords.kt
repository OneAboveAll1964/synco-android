package app.synco.sync

import app.synco.protocol.encoding.Base64Codec
import app.synco.storage.TrustedPeer
import app.synco.transport.PeerDescriptor

internal object TrustedPeerRecords {

    fun of(peer: PeerDescriptor, pairedAtMillis: Long, rejected: Boolean): TrustedPeer = TrustedPeer(
        deviceId = peer.deviceId,
        staticPublicKey = Base64Codec.encode(peer.staticPublicKey),
        displayName = peer.displayName,
        platform = peer.platform,
        firstPairedAtMillis = pairedAtMillis,
        rejected = rejected,
    )

    fun holdsKeyOf(record: TrustedPeer, peer: PeerDescriptor): Boolean =
        record.staticPublicKeyBytes?.contentEquals(peer.staticPublicKey) == true
}
