package app.synco.crypto

import app.synco.protocol.DeviceId
import app.synco.protocol.Fingerprint

class IdentityKeyPair private constructor(private val key: X25519PrivateKey) {

    private val identityHash: ByteArray = CryptoPrimitives.sha256(key.publicKey)

    val publicKey: ByteArray get() = key.publicKey

    val deviceId: DeviceId = DeviceId.fromIdentityHash(identityHash)

    val fingerprint: Fingerprint = Fingerprint.fromIdentityHash(identityHash)

    fun exportPrivateKeyForStorage(): ByteArray = key.exportScalar()

    internal fun agree(peerPublicKey: ByteArray): ByteArray = key.agree(peerPublicKey)

    companion object {
        fun generate(): IdentityKeyPair = IdentityKeyPair(X25519PrivateKey.generate())

        fun fromPrivateKey(bytes: ByteArray): IdentityKeyPair = IdentityKeyPair(X25519PrivateKey.of(bytes))
    }
}
