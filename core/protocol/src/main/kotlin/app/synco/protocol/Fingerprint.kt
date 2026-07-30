package app.synco.protocol

import app.synco.protocol.encoding.Hex
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Fingerprint(val grouped: String) {
    init {
        require(isValidGrouped(grouped)) { "invalid fingerprint '$grouped'" }
    }

    val plain: String get() = grouped.filter { it != ProtocolConstants.FINGERPRINT_SEPARATOR }

    override fun toString(): String = grouped

    companion object {
        fun fromIdentityHash(hash: ByteArray): Fingerprint {
            require(hash.size >= ProtocolConstants.FINGERPRINT_HASH_BYTES) {
                "identity hash must be at least ${ProtocolConstants.FINGERPRINT_HASH_BYTES} bytes"
            }
            return fromPlain(Hex.encodeUpper(hash.copyOf(ProtocolConstants.FINGERPRINT_HASH_BYTES)))
        }

        fun fromPlain(hex: String): Fingerprint = Fingerprint(group(hex.uppercase()))

        fun parseOrNull(raw: String): Fingerprint? {
            val plain = raw.filter { it != ProtocolConstants.FINGERPRINT_SEPARATOR }.uppercase()
            if (plain.length != ProtocolConstants.FINGERPRINT_HEX_LENGTH) return null
            if (!plain.all { it in Hex.UPPERCASE_DIGITS }) return null
            return Fingerprint(group(plain))
        }

        fun isValidGrouped(raw: String): Boolean {
            val groups = raw.split(ProtocolConstants.FINGERPRINT_SEPARATOR)
            if (groups.size != ProtocolConstants.FINGERPRINT_GROUP_COUNT) return false
            return groups.all { group ->
                group.length == ProtocolConstants.FINGERPRINT_GROUP_SIZE &&
                    group.all { it in Hex.UPPERCASE_DIGITS }
            }
        }

        private fun group(plain: String): String = plain
            .chunked(ProtocolConstants.FINGERPRINT_GROUP_SIZE)
            .joinToString(ProtocolConstants.FINGERPRINT_SEPARATOR.toString())
    }
}
