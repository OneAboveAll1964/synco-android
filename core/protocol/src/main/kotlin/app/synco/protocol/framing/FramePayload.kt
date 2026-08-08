package app.synco.protocol.framing

import app.synco.protocol.ProtocolConstants
import app.synco.protocol.SyncoError

class FramePayload(val kind: FrameKind, val body: ByteArray) {

    fun encode(): ByteArray {
        val payload = ByteArray(ProtocolConstants.FRAME_KIND_BYTES + body.size)
        payload[0] = kind.code
        body.copyInto(payload, ProtocolConstants.FRAME_KIND_BYTES)
        return payload
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FramePayload) return false
        return kind == other.kind && body.contentEquals(other.body)
    }

    override fun hashCode(): Int = kind.hashCode() * HASH_MULTIPLIER + body.contentHashCode()

    override fun toString(): String = "FramePayload($kind, ${body.size} bytes)"

    companion object {
        private const val HASH_MULTIPLIER = 31
        private const val BYTE_MASK = 0xFF
        private const val HEX_RADIX = 16

        fun control(body: ByteArray): FramePayload = FramePayload(FrameKind.CONTROL, body)

        fun blob(chunk: BlobChunk): FramePayload = FramePayload(FrameKind.BLOB, chunk.encode())

        fun media(frame: MediaFrame): FramePayload = FramePayload(FrameKind.MEDIA, frame.encode())

        fun decode(payload: ByteArray): FramePayload {
            if (payload.size < ProtocolConstants.FRAME_KIND_BYTES) {
                throw SyncoError.Malformed("a frame payload must carry a kind byte")
            }
            val code = payload[0]
            val kind = FrameKind.fromCode(code) ?: throw SyncoError.Malformed(
                "unknown frame kind 0x${(code.toInt() and BYTE_MASK).toString(HEX_RADIX)}",
            )
            return FramePayload(kind, payload.copyOfRange(ProtocolConstants.FRAME_KIND_BYTES, payload.size))
        }
    }
}
