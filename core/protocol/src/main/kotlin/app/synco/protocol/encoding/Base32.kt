package app.synco.protocol.encoding

object Base32 {
    const val ALPHABET = "abcdefghijklmnopqrstuvwxyz234567"

    private const val BITS_PER_CHAR = 5
    private const val BITS_PER_BYTE = 8
    private const val CHARS_PER_BLOCK = 8
    private const val CHAR_MASK = 0x1FL
    private const val BYTE_MASK = 0xFFL

    private val VALID_TAIL_LENGTHS = setOf(0, 2, 4, 5, 7)

    private val REVERSE = IntArray(128) { -1 }.also { table ->
        ALPHABET.forEachIndexed { index, char -> table[char.code] = index }
    }

    fun encode(bytes: ByteArray): String {
        val text = StringBuilder((bytes.size * BITS_PER_BYTE + BITS_PER_CHAR - 1) / BITS_PER_CHAR)
        var buffer = 0L
        var bits = 0
        for (byte in bytes) {
            buffer = (buffer shl BITS_PER_BYTE) or (byte.toLong() and BYTE_MASK)
            bits += BITS_PER_BYTE
            while (bits >= BITS_PER_CHAR) {
                bits -= BITS_PER_CHAR
                text.append(ALPHABET[((buffer shr bits) and CHAR_MASK).toInt()])
            }
        }
        if (bits > 0) {
            text.append(ALPHABET[((buffer shl (BITS_PER_CHAR - bits)) and CHAR_MASK).toInt()])
        }
        return text.toString()
    }

    fun decode(text: String): ByteArray {
        require(text.length % CHARS_PER_BLOCK in VALID_TAIL_LENGTHS) {
            "base32 input length ${text.length} is not a valid unpadded length"
        }
        val bytes = ByteArray(text.length * BITS_PER_CHAR / BITS_PER_BYTE)
        var index = 0
        var buffer = 0L
        var bits = 0
        for (char in text) {
            val value = valueOf(char)
            require(value >= 0) { "invalid base32 character '$char'" }
            buffer = (buffer shl BITS_PER_CHAR) or value.toLong()
            bits += BITS_PER_CHAR
            if (bits >= BITS_PER_BYTE) {
                bits -= BITS_PER_BYTE
                bytes[index++] = ((buffer shr bits) and BYTE_MASK).toByte()
            }
        }
        if (bits > 0) {
            require((buffer and ((1L shl bits) - 1L)) == 0L) {
                "base32 input has non-zero trailing bits"
            }
        }
        return bytes
    }

    fun isValid(text: String): Boolean = text.all { valueOf(it) >= 0 }

    private fun valueOf(char: Char): Int = if (char.code < REVERSE.size) REVERSE[char.code] else -1
}
