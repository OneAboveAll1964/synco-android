package app.synco.protocol

object HandshakeConstants {
    const val X25519_KEY_BYTES = 32
    const val DH_OUTPUT_BYTES = 32
    const val IKM_BYTES = DH_OUTPUT_BYTES * 3
    const val SALT_BYTES = 32

    const val HKDF_INFO = "synco-v1-session"
    const val SESSION_OKM_BYTES = 64
    const val SESSION_KEY_BYTES = 32

    const val CONFIRM_PREFIX = "synco-v1-confirm"
    const val CONFIRM_TAG_BYTES = 32

    const val AEAD_KEY_BYTES = 32
    const val AEAD_NONCE_BYTES = 12
    const val AEAD_NONCE_PREFIX_BYTES = 4
    const val AEAD_NONCE_COUNTER_BYTES = 8
    const val AEAD_TAG_BYTES = 16
    const val AEAD_TAG_BITS = AEAD_TAG_BYTES * 8
}
