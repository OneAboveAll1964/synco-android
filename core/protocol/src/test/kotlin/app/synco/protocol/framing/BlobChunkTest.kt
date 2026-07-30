package app.synco.protocol.framing

import app.synco.protocol.ProtocolConstants
import app.synco.protocol.SyncoError
import app.synco.protocol.encoding.Hex
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.UUID

class BlobChunkTest {

    @Test
    fun `round trips a chunk`() {
        val chunk = BlobChunk(TRANSFER_ID, offset = 262_144L, data = ByteArray(4096) { (it % 97).toByte() })
        val decoded = BlobChunk.decode(chunk.encode())
        assertEquals(chunk, decoded)
        assertEquals(TRANSFER_ID, decoded.transferId)
        assertEquals(262_144L, decoded.offset)
    }

    @Test
    fun `lays out the transfer id then the offset then the data`() {
        val chunk = BlobChunk(TRANSFER_ID, offset = 1L, data = byteArrayOf(0x7F))
        val body = chunk.encode()
        assertEquals(ProtocolConstants.BLOB_HEADER_BYTES + 1, body.size)
        assertEquals(
            TRANSFER_ID.toString().replace("-", ""),
            Hex.encodeLower(body.copyOf(ProtocolConstants.BLOB_TRANSFER_ID_BYTES)),
        )
        assertArrayEquals(
            byteArrayOf(0, 0, 0, 0, 0, 0, 0, 1),
            body.copyOfRange(ProtocolConstants.BLOB_TRANSFER_ID_BYTES, ProtocolConstants.BLOB_HEADER_BYTES),
        )
        assertArrayEquals(byteArrayOf(0x7F), body.copyOfRange(ProtocolConstants.BLOB_HEADER_BYTES, body.size))
    }

    @Test
    fun `round trips a zero length chunk`() {
        val chunk = BlobChunk(TRANSFER_ID, offset = 0L, data = ByteArray(0))
        assertEquals(chunk, BlobChunk.decode(chunk.encode()))
    }

    @Test
    fun `round trips a chunk of exactly the maximum size`() {
        val chunk = BlobChunk(TRANSFER_ID, 0L, ByteArray(ProtocolConstants.MAX_BLOB_CHUNK_BYTES) { 0x2A })
        assertEquals(chunk, BlobChunk.decode(chunk.encode()))
    }

    @Test
    fun `rejects a body shorter than the header`() {
        assertThrows(SyncoError.Malformed::class.java) {
            BlobChunk.decode(ByteArray(ProtocolConstants.BLOB_HEADER_BYTES - 1))
        }
    }

    @Test
    fun `rejects an oversized chunk`() {
        assertThrows(IllegalArgumentException::class.java) {
            BlobChunk(TRANSFER_ID, 0L, ByteArray(ProtocolConstants.MAX_BLOB_CHUNK_BYTES + 1))
        }
    }

    private companion object {
        val TRANSFER_ID: UUID = UUID.fromString("3f2a1b0c-4d5e-6f70-8192-a3b4c5d6e7f8")
    }
}
