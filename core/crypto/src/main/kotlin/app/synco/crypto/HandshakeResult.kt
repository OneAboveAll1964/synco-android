package app.synco.crypto

import app.synco.protocol.SyncoError

class HandshakeResult internal constructor(
    val keys: SessionKeys,
    private val confirmation: ByteArray,
    private val expectedPeerConfirmation: ByteArray,
) {
    val confirmationTag: ByteArray get() = confirmation.copyOf()

    fun verifyPeerTag(tag: ByteArray): Boolean =
        CryptoPrimitives.constantTimeEquals(expectedPeerConfirmation, tag)

    fun requirePeerTag(tag: ByteArray) {
        if (!verifyPeerTag(tag)) {
            throw SyncoError.BadAuth("the peer confirmation tag does not match the derived session key")
        }
    }

    fun ciphers(): SessionCipherPair = SessionCipherPair(keys)
}
