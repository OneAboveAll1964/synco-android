package app.synco.protocol.framing

import app.synco.protocol.ProtocolConstants

class FrameDecoder(private val initialCapacity: Int = DEFAULT_CAPACITY) {

    init {
        require(initialCapacity >= ProtocolConstants.FRAME_LENGTH_PREFIX_BYTES) {
            "a frame decoder needs room for the length prefix"
        }
    }

    private var buffer = ByteArray(initialCapacity)
    private var readIndex = 0
    private var writeIndex = 0
    private var pendingLength = NO_PENDING_FRAME

    val bufferedBytes: Int get() = writeIndex - readIndex

    fun feed(chunk: ByteArray, offset: Int = 0, length: Int = chunk.size - offset) {
        require(offset >= 0 && length >= 0 && offset + length <= chunk.size) { "chunk range out of bounds" }
        if (length == 0) return
        ensureWritable(length)
        chunk.copyInto(buffer, writeIndex, offset, offset + length)
        writeIndex += length
    }

    fun next(): ByteArray? {
        if (pendingLength == NO_PENDING_FRAME) {
            if (bufferedBytes < ProtocolConstants.FRAME_LENGTH_PREFIX_BYTES) return null
            pendingLength = FrameCodec.validateLength(FrameCodec.readLengthPrefix(buffer, readIndex))
            readIndex += ProtocolConstants.FRAME_LENGTH_PREFIX_BYTES
        }
        if (bufferedBytes < pendingLength) return null
        val payload = buffer.copyOfRange(readIndex, readIndex + pendingLength)
        readIndex += pendingLength
        pendingLength = NO_PENDING_FRAME
        if (readIndex == writeIndex) reset()
        return payload
    }

    fun reset() {
        readIndex = 0
        writeIndex = 0
        pendingLength = NO_PENDING_FRAME
        if (buffer.size > initialCapacity) buffer = ByteArray(initialCapacity)
    }

    private fun ensureWritable(length: Int) {
        if (writeIndex + length <= buffer.size) return
        compact()
        if (writeIndex + length <= buffer.size) return
        var capacity = buffer.size
        while (capacity < writeIndex + length) capacity *= GROWTH_FACTOR
        buffer = buffer.copyOf(capacity)
    }

    private fun compact() {
        if (readIndex == 0) return
        buffer.copyInto(buffer, 0, readIndex, writeIndex)
        writeIndex -= readIndex
        readIndex = 0
    }

    private companion object {
        const val DEFAULT_CAPACITY = 8192
        const val GROWTH_FACTOR = 2
        const val NO_PENDING_FRAME = -1
    }
}
