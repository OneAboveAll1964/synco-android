package app.synco.sync

import app.synco.protocol.DeviceId
import app.synco.storage.TrustedPeerStore
import app.synco.transport.TrustedPeers

class TrustedPeerKeys(private val store: TrustedPeerStore) : TrustedPeers {

    override suspend fun staticPublicKey(deviceId: DeviceId): ByteArray? =
        store.find(deviceId)?.takeIf { it.isTrusted }?.staticPublicKeyBytes
}
