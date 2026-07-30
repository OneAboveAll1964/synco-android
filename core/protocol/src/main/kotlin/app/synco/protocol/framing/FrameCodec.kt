package app.synco.protocol.framing

import app.synco.protocol.ProtocolConstants
import app.synco.protocol.SyncoError

object FrameCodec {
    private const val BYTE_MASK = 0xFFL
    private const val BITS_PER_BYTE = 8

    fun encode(payload: ByteArray): ByteArray {
        if (payload.size < ProtocolConstants.MIN_FRAME_PAYLOAD_BYTES) {
            throw SyncoError.Malformed("a frame payload may not be empty")
        }
        if (payload.size > ProtocolConstants.MAX_FRAME_PAYLOAD_BYTES) {
            throw SyncoError.FrameTooLarge(payload.size.toLong())
        }
        val frame = ByteArray(ProtocolConstants.FRAME_LENGTH_PREFIX_BYTES + payload.size)
        writeLengthPrefix(frame, 0, payload.size)
        payload.copyInto(frame, ProtocolConstants.FRAME_LENGTH_PREFIX_BYTES)
        return frame
    }

    fun encodeLengthPrefix(length: Int): ByteArray =
        ByteArray(ProtocolConstants.FRAME_LENGTH_PREFIX_BYTES).also { writeLengthPrefix(it, 0, length) }

    fun writeLengthPrefix(target: ByteArray, offset: Int, length: Int) {
        for (index in 0 until ProtocolConstants.FRAME_LENGTH_PREFIX_BYTES) {
            val shift = (ProtocolConstants.FRAME_LENGTH_PREFIX_BYTES - 1 - index) * BITS_PER_BYTE
            target[offset + index] = ((length.toLong() shr shift) and BYTE_MASK).toByte()
        }
    }

    fun readLengthPrefix(source: ByteArray, offset: Int = 0): Long {
        var length = 0L
        for (index in 0 until ProtocolConstants.FRAME_LENGTH_PREFIX_BYTES) {
            length = (length shl BITS_PER_BYTE) or (source[offset + index].toLong() and BYTE_MASK)
        }
        return length
    }

    fun validateLength(length: Long): Int {
        if (length < ProtocolConstants.MIN_FRAME_PAYLOAD_BYTES) {
            throw SyncoError.Malformed("a frame length of $length is invalid")
        }
        if (length > ProtocolConstants.MAX_FRAME_PAYLOAD_BYTES) {
            throw SyncoError.FrameTooLarge(length)
        }
        return length.toInt()
    }
}
