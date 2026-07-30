package app.synco.protocol

import app.synco.protocol.encoding.Base32
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class DeviceId(val value: String) : Comparable<DeviceId> {
    init {
        require(isValid(value)) { "invalid device id '$value'" }
    }

    override fun compareTo(other: DeviceId): Int = value.compareTo(other.value)

    override fun toString(): String = value

    companion object {
        fun fromIdentityHash(hash: ByteArray): DeviceId {
            require(hash.size >= ProtocolConstants.DEVICE_ID_HASH_BYTES) {
                "identity hash must be at least ${ProtocolConstants.DEVICE_ID_HASH_BYTES} bytes"
            }
            return DeviceId(Base32.encode(hash.copyOf(ProtocolConstants.DEVICE_ID_HASH_BYTES)))
        }

        fun parseOrNull(raw: String): DeviceId? = if (isValid(raw)) DeviceId(raw) else null

        fun isValid(raw: String): Boolean =
            raw.length == ProtocolConstants.DEVICE_ID_LENGTH && Base32.isValid(raw)
    }
}
