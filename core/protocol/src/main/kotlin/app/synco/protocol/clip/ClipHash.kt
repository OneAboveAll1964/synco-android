package app.synco.protocol.clip

import app.synco.protocol.ProtocolConstants
import app.synco.protocol.encoding.Base64Codec
import app.synco.protocol.encoding.Hex
import app.synco.protocol.message.ClipRep
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

object ClipHash {
    private const val DIGEST_ALGORITHM = "SHA-256"
    private val UNIT_SEPARATOR = ProtocolConstants.CANONICAL_UNIT_SEPARATOR.toInt()
    private val RECORD_SEPARATOR = ProtocolConstants.CANONICAL_RECORD_SEPARATOR.toInt()
    private val ESCAPE = ProtocolConstants.CANONICAL_ESCAPE.toInt()
    private const val BYTE_MASK = 0xFF

    fun compute(reps: List<ClipRep>): String =
        Hex.encodeLower(MessageDigest.getInstance(DIGEST_ALGORITHM).digest(canonicalBytes(reps)))

    fun canonicalBytes(reps: List<ClipRep>): ByteArray {
        val canonical = ByteArrayOutputStream()
        for (rep in reps) {
            canonical.write(utf8(rep.kind))
            canonical.write(UNIT_SEPARATOR)
            canonical.write(repBytes(rep))
            canonical.write(RECORD_SEPARATOR)
        }
        return canonical.toByteArray()
    }

    private fun repBytes(rep: ClipRep): ByteArray = when (rep) {
        is ClipRep.Text -> escape(utf8(rep.text))
        is ClipRep.Html -> escape(utf8(rep.html))
        is ClipRep.Rtf -> escape(Base64Codec.decode(rep.base64))
        is ClipRep.Url -> escape(utf8(rep.url))
        is ClipRep.Image -> escape(utf8(rep.sha256))
        is ClipRep.File -> fileRepBytes(rep)
    }

    private fun fileRepBytes(rep: ClipRep.File): ByteArray {
        val bytes = ByteArrayOutputStream()
        bytes.write(escape(utf8(rep.name)))
        bytes.write(UNIT_SEPARATOR)
        bytes.write(escape(utf8(rep.sha256)))
        return bytes.toByteArray()
    }

    private fun escape(value: ByteArray): ByteArray {
        val escaped = ByteArrayOutputStream(value.size)
        for (byte in value) {
            val current = byte.toInt() and BYTE_MASK
            if (current == ESCAPE || current == UNIT_SEPARATOR || current == RECORD_SEPARATOR) {
                escaped.write(ESCAPE)
            }
            escaped.write(current)
        }
        return escaped.toByteArray()
    }

    private fun utf8(value: String): ByteArray = value.toByteArray(Charsets.UTF_8)
}
