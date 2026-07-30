package app.synco.crypto

class SessionCipherPair(keys: SessionKeys) {
    val outbound: SessionCipher = SessionCipher(keys.sendKey)
    val inbound: SessionCipher = SessionCipher(keys.receiveKey)

    fun seal(payload: ByteArray): ByteArray = outbound.seal(payload)

    fun open(record: ByteArray): ByteArray = inbound.open(record)
}
