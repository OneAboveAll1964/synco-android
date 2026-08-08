package app.synco.protocol.framing

import app.synco.protocol.SyncoError

class MediaFrame(
    val stream: Int,
    val flags: Int,
    val seq: Long,
    val ptsMicros: Long,
    val data: ByteArray,
) {
    val isKeyframe: Boolean get() = flags and KEYFRAME != 0
    val isConfig: Boolean get() = flags and CONFIG != 0
    val isLastFragment: Boolean get() = flags and LAST_FRAGMENT != 0

    fun encode(): ByteArray {
        val body = ByteArray(HEADER_BYTES + data.size)
        body[0] = stream.toByte()
        body[1] = flags.toByte()
        writeUInt32(body, STREAM_BYTES + FLAGS_BYTES, seq)
        writeUInt64(body, STREAM_BYTES + FLAGS_BYTES + SEQ_BYTES, ptsMicros)
        data.copyInto(body, HEADER_BYTES)
        return body
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MediaFrame) return false
        return stream == other.stream && flags == other.flags && seq == other.seq &&
            ptsMicros == other.ptsMicros && data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = stream
        result = result * HASH_MULTIPLIER + flags
        result = result * HASH_MULTIPLIER + seq.hashCode()
        result = result * HASH_MULTIPLIER + ptsMicros.hashCode()
        result = result * HASH_MULTIPLIER + data.contentHashCode()
        return result
    }

    companion object {
        const val KEYFRAME = 0x01
        const val CONFIG = 0x02
        const val LAST_FRAGMENT = 0x04

        private const val STREAM_BYTES = 1
        private const val FLAGS_BYTES = 1
        private const val SEQ_BYTES = 4
        private const val PTS_BYTES = 8
        const val HEADER_BYTES = STREAM_BYTES + FLAGS_BYTES + SEQ_BYTES + PTS_BYTES

        private const val BYTE_MASK = 0xFFL
        private const val BITS_PER_BYTE = 8
        private const val HASH_MULTIPLIER = 31

        fun decode(body: ByteArray): MediaFrame {
            if (body.size < HEADER_BYTES) {
                throw SyncoError.Malformed("a media frame must carry a ${HEADER_BYTES}-byte header")
            }
            return MediaFrame(
                stream = body[0].toInt() and 0xFF,
                flags = body[1].toInt() and 0xFF,
                seq = readUnsigned(body, STREAM_BYTES + FLAGS_BYTES, SEQ_BYTES),
                ptsMicros = readUnsigned(body, STREAM_BYTES + FLAGS_BYTES + SEQ_BYTES, PTS_BYTES),
                data = body.copyOfRange(HEADER_BYTES, body.size),
            )
        }

        private fun writeUInt32(target: ByteArray, offset: Int, value: Long) {
            for (index in 0 until SEQ_BYTES) {
                target[offset + index] = (value shr ((SEQ_BYTES - 1 - index) * BITS_PER_BYTE)).toByte()
            }
        }

        private fun writeUInt64(target: ByteArray, offset: Int, value: Long) {
            for (index in 0 until PTS_BYTES) {
                target[offset + index] = (value shr ((PTS_BYTES - 1 - index) * BITS_PER_BYTE)).toByte()
            }
        }

        private fun readUnsigned(source: ByteArray, offset: Int, length: Int): Long {
            var value = 0L
            for (index in 0 until length) {
                value = (value shl BITS_PER_BYTE) or (source[offset + index].toLong() and BYTE_MASK)
            }
            return value
        }
    }
}
