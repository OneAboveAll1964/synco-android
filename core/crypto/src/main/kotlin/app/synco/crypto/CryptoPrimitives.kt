package app.synco.crypto

import app.synco.protocol.HandshakeConstants
import app.synco.protocol.SyncoError
import org.bouncycastle.crypto.agreement.X25519Agreement
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.bouncycastle.util.Arrays
import java.security.SecureRandom

object CryptoPrimitives {
    private val secureRandom = SecureRandom()

    fun randomBytes(length: Int): ByteArray = ByteArray(length).also(secureRandom::nextBytes)

    fun sha256(vararg inputs: ByteArray): ByteArray {
        val digest = SHA256Digest()
        inputs.forEach { digest.update(it, 0, it.size) }
        return ByteArray(digest.digestSize).also { digest.doFinal(it, 0) }
    }

    fun hmacSha256(key: ByteArray, message: ByteArray): ByteArray {
        val mac = HMac(SHA256Digest())
        mac.init(KeyParameter(key))
        mac.update(message, 0, message.size)
        return ByteArray(mac.macSize).also { mac.doFinal(it, 0) }
    }

    fun hkdfSha256(ikm: ByteArray, salt: ByteArray, info: ByteArray, length: Int): ByteArray {
        require(length > 0) { "hkdf output length must be positive" }
        val generator = HKDFBytesGenerator(SHA256Digest())
        generator.init(HKDFParameters(ikm, salt, info))
        return ByteArray(length).also { generator.generateBytes(it, 0, length) }
    }

    fun x25519(privateKey: ByteArray, peerPublicKey: ByteArray): ByteArray {
        requireKeySize(privateKey, "private key")
        requireKeySize(peerPublicKey, "peer public key")
        val shared = ByteArray(HandshakeConstants.DH_OUTPUT_BYTES)
        try {
            val agreement = X25519Agreement()
            agreement.init(X25519PrivateKeyParameters(privateKey))
            agreement.calculateAgreement(X25519PublicKeyParameters(peerPublicKey), shared, 0)
        } catch (error: IllegalStateException) {
            throw SyncoError.BadHandshake("x25519 agreement failed: ${error.message}")
        } catch (error: IllegalArgumentException) {
            throw SyncoError.BadHandshake("x25519 agreement failed: ${error.message}")
        }
        if (isAllZero(shared)) {
            throw SyncoError.BadHandshake("x25519 produced an all-zero shared secret")
        }
        return shared
    }

    fun constantTimeEquals(expected: ByteArray, actual: ByteArray): Boolean =
        Arrays.constantTimeAreEqual(expected, actual)

    fun isAllZero(bytes: ByteArray): Boolean {
        var accumulator = 0
        for (byte in bytes) accumulator = accumulator or byte.toInt()
        return accumulator == 0
    }

    private fun requireKeySize(key: ByteArray, label: String) {
        require(key.size == HandshakeConstants.X25519_KEY_BYTES) {
            "$label must be ${HandshakeConstants.X25519_KEY_BYTES} bytes, was ${key.size}"
        }
    }
}
