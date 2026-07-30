package app.synco.protocol.encoding

object Hex {
    const val LOWERCASE_DIGITS = "0123456789abcdef"
    const val UPPERCASE_DIGITS = "0123456789ABCDEF"

    private const val HIGH_NIBBLE_SHIFT = 4
    private const val NIBBLE_MASK = 0x0F

    fun encodeLower(bytes: ByteArray): String = encode(bytes, LOWERCASE_DIGITS)

    fun encodeUpper(bytes: ByteArray): String = encode(bytes, UPPERCASE_DIGITS)

    fun decode(text: String): ByteArray {
        require(text.length % 2 == 0) { "hex input must have an even length" }
        val bytes = ByteArray(text.length / 2)
        for (index in bytes.indices) {
            val high = nibbleOf(text[index * 2])
            val low = nibbleOf(text[index * 2 + 1])
            bytes[index] = ((high shl HIGH_NIBBLE_SHIFT) or low).toByte()
        }
        return bytes
    }

    fun isValid(text: String): Boolean = text.length % 2 == 0 && text.all { digitValue(it) >= 0 }

    private fun encode(bytes: ByteArray, digits: String): String {
        val text = StringBuilder(bytes.size * 2)
        for (byte in bytes) {
            val value = byte.toInt()
            text.append(digits[(value shr HIGH_NIBBLE_SHIFT) and NIBBLE_MASK])
            text.append(digits[value and NIBBLE_MASK])
        }
        return text.toString()
    }

    private fun nibbleOf(char: Char): Int {
        val value = digitValue(char)
        require(value >= 0) { "invalid hex character '$char'" }
        return value
    }

    private fun digitValue(char: Char): Int = when (char) {
        in '0'..'9' -> char - '0'
        in 'a'..'f' -> char - 'a' + 10
        in 'A'..'F' -> char - 'A' + 10
        else -> -1
    }
}
