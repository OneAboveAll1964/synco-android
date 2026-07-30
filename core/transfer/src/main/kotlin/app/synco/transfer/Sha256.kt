package app.synco.transfer

import app.synco.protocol.encoding.Hex
import java.io.InputStream
import java.security.MessageDigest

object Sha256 {
    const val ALGORITHM = "SHA-256"

    private const val BUFFER_BYTES = 65_536

    fun newDigest(): MessageDigest = MessageDigest.getInstance(ALGORITHM)

    fun hexOf(digest: MessageDigest): String = Hex.encodeLower(digest.digest())

    fun of(input: InputStream): BlobDigest {
        val digest = newDigest()
        val buffer = ByteArray(BUFFER_BYTES)
        var size = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            digest.update(buffer, 0, read)
            size += read
        }
        return BlobDigest(hexOf(digest), size)
    }

    fun of(bytes: ByteArray): BlobDigest =
        BlobDigest(Hex.encodeLower(newDigest().digest(bytes)), bytes.size.toLong())
}
