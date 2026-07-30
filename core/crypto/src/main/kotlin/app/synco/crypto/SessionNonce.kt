package app.synco.crypto

import app.synco.protocol.HandshakeConstants

internal object SessionNonce {
    private const val BYTE_MASK = 0xFFL
    private const val BITS_PER_BYTE = 8

    fun forCounter(counter: ULong): ByteArray {
        val nonce = ByteArray(HandshakeConstants.AEAD_NONCE_BYTES)
        val value = counter.toLong()
        for (index in 0 until HandshakeConstants.AEAD_NONCE_COUNTER_BYTES) {
            val shift = (HandshakeConstants.AEAD_NONCE_COUNTER_BYTES - 1 - index) * BITS_PER_BYTE
            nonce[HandshakeConstants.AEAD_NONCE_PREFIX_BYTES + index] =
                ((value shr shift) and BYTE_MASK).toByte()
        }
        return nonce
    }
}
