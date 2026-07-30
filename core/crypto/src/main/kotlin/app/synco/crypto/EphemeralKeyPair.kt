package app.synco.crypto

class EphemeralKeyPair private constructor(private val key: X25519PrivateKey) {

    val publicKey: ByteArray get() = key.publicKey

    internal fun agree(peerPublicKey: ByteArray): ByteArray = key.agree(peerPublicKey)

    companion object {
        fun generate(): EphemeralKeyPair = EphemeralKeyPair(X25519PrivateKey.generate())

        internal fun fromPrivateKey(bytes: ByteArray): EphemeralKeyPair =
            EphemeralKeyPair(X25519PrivateKey.of(bytes))
    }
}
