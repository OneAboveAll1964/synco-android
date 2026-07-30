package app.synco.crypto

import app.synco.protocol.HandshakeConstants
import app.synco.protocol.SyncoError
import org.bouncycastle.crypto.InvalidCipherTextException
import org.bouncycastle.crypto.modes.ChaCha20Poly1305
import org.bouncycastle.crypto.params.AEADParameters
import org.bouncycastle.crypto.params.KeyParameter

class SessionCipher(key: ByteArray) {

    private val key: ByteArray

    var counter: ULong = 0uL
        private set

    init {
        require(key.size == HandshakeConstants.AEAD_KEY_BYTES) {
            "a session key must be ${HandshakeConstants.AEAD_KEY_BYTES} bytes, was ${key.size}"
        }
        this.key = key.copyOf()
    }

    fun seal(plaintext: ByteArray): ByteArray {
        val record = transform(forEncryption = true, nonce = nextNonce(), input = plaintext)
        counter++
        return record
    }

    fun open(record: ByteArray): ByteArray {
        if (record.size < HandshakeConstants.AEAD_TAG_BYTES) {
            throw SyncoError.Malformed("a record shorter than ${HandshakeConstants.AEAD_TAG_BYTES} bytes has no tag")
        }
        val nonce = nextNonce()
        val plaintext = try {
            transform(forEncryption = false, nonce = nonce, input = record)
        } catch (error: InvalidCipherTextException) {
            throw SyncoError.Replay(counter)
        }
        counter++
        return plaintext
    }

    private fun nextNonce(): ByteArray {
        if (counter == ULong.MAX_VALUE) throw SyncoError.CounterExhausted()
        return SessionNonce.forCounter(counter)
    }

    private fun transform(forEncryption: Boolean, nonce: ByteArray, input: ByteArray): ByteArray {
        val cipher = ChaCha20Poly1305()
        cipher.init(forEncryption, AEADParameters(KeyParameter(key), HandshakeConstants.AEAD_TAG_BITS, nonce))
        val output = ByteArray(cipher.getOutputSize(input.size))
        var written = cipher.processBytes(input, 0, input.size, output, 0)
        written += cipher.doFinal(output, written)
        return if (written == output.size) output else output.copyOf(written)
    }
}
