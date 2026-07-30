package app.synco.crypto

import app.synco.protocol.HandshakeConstants
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters

internal class X25519PrivateKey private constructor(private val scalar: ByteArray) {

    private val encodedPublicKey: ByteArray =
        X25519PrivateKeyParameters(scalar).generatePublicKey().encoded

    val publicKey: ByteArray get() = encodedPublicKey.copyOf()

    fun agree(peerPublicKey: ByteArray): ByteArray = CryptoPrimitives.x25519(scalar, peerPublicKey)

    fun exportScalar(): ByteArray = scalar.copyOf()

    companion object {
        fun generate(): X25519PrivateKey =
            X25519PrivateKey(CryptoPrimitives.randomBytes(HandshakeConstants.X25519_KEY_BYTES))

        fun of(bytes: ByteArray): X25519PrivateKey {
            require(bytes.size == HandshakeConstants.X25519_KEY_BYTES) {
                "an x25519 scalar must be ${HandshakeConstants.X25519_KEY_BYTES} bytes, was ${bytes.size}"
            }
            return X25519PrivateKey(bytes.copyOf())
        }
    }
}
