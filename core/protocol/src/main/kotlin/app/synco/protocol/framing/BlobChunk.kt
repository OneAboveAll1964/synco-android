package app.synco.protocol.framing

import app.synco.protocol.ProtocolConstants
import app.synco.protocol.SyncoError
import java.util.UUID

class BlobChunk(val transferId: UUID, val offset: Long, val data: ByteArray) {

    init {
        require(data.size <= ProtocolConstants.MAX_BLOB_CHUNK_BYTES) {
            "a blob chunk may not exceed ${ProtocolConstants.MAX_BLOB_CHUNK_BYTES} bytes"
        }
    }

    fun encode(): ByteArray {
        val body = ByteArray(ProtocolConstants.BLOB_HEADER_BYTES + data.size)
        writeLong(body, 0, transferId.mostSignificantBits)
        writeLong(body, Long.SIZE_BYTES, transferId.leastSignificantBits)
        writeLong(body, ProtocolConstants.BLOB_TRANSFER_ID_BYTES, offset)
        data.copyInto(body, ProtocolConstants.BLOB_HEADER_BYTES)
        return body
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlobChunk) return false
        return transferId == other.transferId && offset == other.offset && data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = transferId.hashCode()
        result = result * HASH_MULTIPLIER + offset.hashCode()
        result = result * HASH_MULTIPLIER + data.contentHashCode()
        return result
    }

    override fun toString(): String = "BlobChunk($transferId, offset=$offset, ${data.size} bytes)"

    companion object {
        private const val HASH_MULTIPLIER = 31
        private const val BYTE_MASK = 0xFFL
        private const val BITS_PER_BYTE = 8

        fun decode(body: ByteArray): BlobChunk {
            if (body.size < ProtocolConstants.BLOB_HEADER_BYTES) {
                throw SyncoError.Malformed("a blob chunk needs ${ProtocolConstants.BLOB_HEADER_BYTES} header bytes")
            }
            val transferId = UUID(readLong(body, 0), readLong(body, Long.SIZE_BYTES))
            val offset = readLong(body, ProtocolConstants.BLOB_TRANSFER_ID_BYTES)
            if (offset < 0) throw SyncoError.Malformed("a blob offset beyond 2^63 is unsupported")
            val data = body.copyOfRange(ProtocolConstants.BLOB_HEADER_BYTES, body.size)
            if (data.size > ProtocolConstants.MAX_BLOB_CHUNK_BYTES) {
                throw SyncoError.Malformed("a blob chunk of ${data.size} bytes exceeds the maximum")
            }
            return BlobChunk(transferId, offset, data)
        }

        private fun writeLong(target: ByteArray, offset: Int, value: Long) {
            for (index in 0 until Long.SIZE_BYTES) {
                val shift = (Long.SIZE_BYTES - 1 - index) * BITS_PER_BYTE
                target[offset + index] = ((value shr shift) and BYTE_MASK).toByte()
            }
        }

        private fun readLong(source: ByteArray, offset: Int): Long {
            var value = 0L
            for (index in 0 until Long.SIZE_BYTES) {
                value = (value shl BITS_PER_BYTE) or (source[offset + index].toLong() and BYTE_MASK)
            }
            return value
        }
    }
}
