package app.synco.crypto

import app.synco.protocol.HandshakeConstants
import app.synco.protocol.SyncoError
import app.synco.protocol.encoding.Hex
import org.bouncycastle.crypto.modes.ChaCha20Poly1305
import org.bouncycastle.crypto.params.AEADParameters
import org.bouncycastle.crypto.params.KeyParameter
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SessionCipherTest {

    @Test
    fun `round trips a sequence of records across a cipher pair`() {
        val sender = SessionCipher(KEY)
        val receiver = SessionCipher(KEY)
        val payloads = List(8) { index -> ByteArray(index * 37 + 1) { (index + it).toByte() } }
        payloads.forEach { payload ->
            assertArrayEquals(payload, receiver.open(sender.seal(payload)))
        }
        assertEquals(payloads.size.toLong(), sender.counter.toLong())
        assertEquals(payloads.size.toLong(), receiver.counter.toLong())
    }

    @Test
    fun `appends a sixteen byte tag to the ciphertext`() {
        val payload = ByteArray(64) { 0x33 }
        val record = SessionCipher(KEY).seal(payload)
        assertEquals(payload.size + HandshakeConstants.AEAD_TAG_BYTES, record.size)
    }

    @Test
    fun `numbers records from zero with a four byte zero prefix and a big endian counter`() {
        assertArrayEquals(ByteArray(HandshakeConstants.AEAD_NONCE_BYTES), SessionNonce.forCounter(0uL))
        assertEquals("000000000000000000000001", Hex.encodeLower(SessionNonce.forCounter(1uL)))
        assertEquals("000000000000000000000100", Hex.encodeLower(SessionNonce.forCounter(256uL)))
        assertEquals("00000000ffffffffffffffff", Hex.encodeLower(SessionNonce.forCounter(ULong.MAX_VALUE)))
    }

    @Test
    fun `seals with the nonce for the current counter`() {
        val cipher = SessionCipher(KEY)
        val payload = "control frame".toByteArray()
        assertArrayEquals(referenceSeal(nonceFor(0), payload), cipher.seal(payload))
        assertArrayEquals(referenceSeal(nonceFor(1), payload), cipher.seal(payload))
        assertArrayEquals(referenceSeal(nonceFor(2), payload), cipher.seal(payload))
    }

    @Test
    fun `rejects a replayed record`() {
        val sender = SessionCipher(KEY)
        val receiver = SessionCipher(KEY)
        val first = sender.seal("one".toByteArray())
        assertArrayEquals("one".toByteArray(), receiver.open(first))
        assertThrows(SyncoError.Replay::class.java) { receiver.open(first) }
    }

    @Test
    fun `rejects a reordered record`() {
        val sender = SessionCipher(KEY)
        val receiver = SessionCipher(KEY)
        sender.seal("one".toByteArray())
        val second = sender.seal("two".toByteArray())
        assertThrows(SyncoError.Replay::class.java) { receiver.open(second) }
    }

    @Test
    fun `rejects a tampered record`() {
        val sender = SessionCipher(KEY)
        val receiver = SessionCipher(KEY)
        val record = sender.seal("payload".toByteArray()).also { it[0] = (it[0] + 1).toByte() }
        assertThrows(SyncoError.Replay::class.java) { receiver.open(record) }
    }

    @Test
    fun `rejects a record opened with the wrong direction key`() {
        val sender = SessionCipher(KEY)
        val receiver = SessionCipher(OTHER_KEY)
        assertThrows(SyncoError.Replay::class.java) { receiver.open(sender.seal("payload".toByteArray())) }
    }

    @Test
    fun `rejects a record too short to carry a tag`() {
        assertThrows(SyncoError.Malformed::class.java) {
            SessionCipher(KEY).open(ByteArray(HandshakeConstants.AEAD_TAG_BYTES - 1))
        }
    }

    @Test
    fun `rejects a key of the wrong length`() {
        assertThrows(IllegalArgumentException::class.java) { SessionCipher(ByteArray(16)) }
    }

    @Test
    fun `pairs opposing ciphers for a session`() {
        val keys = SessionKeys(KEY, OTHER_KEY)
        val local = SessionCipherPair(keys)
        val remote = SessionCipherPair(SessionKeys(OTHER_KEY, KEY))
        assertArrayEquals("hi".toByteArray(), remote.open(local.seal("hi".toByteArray())))
        assertArrayEquals("back".toByteArray(), local.open(remote.seal("back".toByteArray())))
    }

    private fun nonceFor(counter: Int): ByteArray =
        ByteArray(HandshakeConstants.AEAD_NONCE_BYTES).also {
            it[HandshakeConstants.AEAD_NONCE_BYTES - 1] = counter.toByte()
        }

    private fun referenceSeal(nonce: ByteArray, plaintext: ByteArray): ByteArray {
        val cipher = ChaCha20Poly1305()
        cipher.init(true, AEADParameters(KeyParameter(KEY), HandshakeConstants.AEAD_TAG_BITS, nonce))
        val output = ByteArray(cipher.getOutputSize(plaintext.size))
        var written = cipher.processBytes(plaintext, 0, plaintext.size, output, 0)
        written += cipher.doFinal(output, written)
        return output.copyOf(written)
    }

    private companion object {
        val KEY = ByteArray(HandshakeConstants.AEAD_KEY_BYTES) { (it * 7 + 1).toByte() }
        val OTHER_KEY = ByteArray(HandshakeConstants.AEAD_KEY_BYTES) { (it * 13 + 5).toByte() }
    }
}
