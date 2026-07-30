package app.synco.crypto

import app.synco.protocol.HandshakeConstants

class SessionKeys internal constructor(send: ByteArray, receive: ByteArray) {

    private val send: ByteArray
    private val receive: ByteArray

    init {
        requireSessionKeySize(send)
        requireSessionKeySize(receive)
        this.send = send.copyOf()
        this.receive = receive.copyOf()
    }

    val sendKey: ByteArray get() = send.copyOf()

    val receiveKey: ByteArray get() = receive.copyOf()

    private fun requireSessionKeySize(key: ByteArray) {
        require(key.size == HandshakeConstants.SESSION_KEY_BYTES) {
            "a session key must be ${HandshakeConstants.SESSION_KEY_BYTES} bytes, was ${key.size}"
        }
    }
}
