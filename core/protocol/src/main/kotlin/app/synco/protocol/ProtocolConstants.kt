package app.synco.protocol

object ProtocolConstants {
    const val VERSION = 1
    const val SERVICE_NAME = "synco"

    const val DEVICE_ID_HASH_BYTES = 10
    const val DEVICE_ID_LENGTH = 16

    const val FINGERPRINT_HASH_BYTES = 8
    const val FINGERPRINT_HEX_LENGTH = 16
    const val FINGERPRINT_GROUP_SIZE = 4
    const val FINGERPRINT_GROUP_COUNT = FINGERPRINT_HEX_LENGTH / FINGERPRINT_GROUP_SIZE
    const val FINGERPRINT_SEPARATOR = '-'

    const val FRAME_LENGTH_PREFIX_BYTES = 4
    const val FRAME_KIND_BYTES = 1
    const val MIN_FRAME_PAYLOAD_BYTES = 1
    const val MAX_FRAME_PAYLOAD_BYTES = 1_048_576

    const val BLOB_TRANSFER_ID_BYTES = 16
    const val BLOB_OFFSET_BYTES = 8
    const val BLOB_HEADER_BYTES = BLOB_TRANSFER_ID_BYTES + BLOB_OFFSET_BYTES
    const val MAX_BLOB_CHUNK_BYTES = 262_144

    const val INLINE_REP_MAX_BYTES = 65_536
    const val DEFAULT_MAX_BLOB_BYTES = 104_857_600L

    const val PING_INTERVAL_MILLIS = 15_000L
    const val READ_TIMEOUT_MILLIS = 45_000L
    const val PAIR_TIMEOUT_MILLIS = 120_000L

    const val SUPPRESSION_WINDOW_MILLIS = 10_000L
    const val SUPPRESSION_MAX_ENTRIES = 32

    const val CANONICAL_UNIT_SEPARATOR: Byte = 0x1F
    const val CANONICAL_RECORD_SEPARATOR: Byte = 0x1E
    const val CANONICAL_ESCAPE: Byte = 0x1D
}
