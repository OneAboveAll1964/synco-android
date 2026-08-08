package app.synco.remote

object HidKeyboard {

    const val USAGE_A = 0x04
    const val USAGE_1 = 0x1E
    const val USAGE_ENTER = 0x28
    const val USAGE_ESCAPE = 0x29
    const val USAGE_BACKSPACE = 0x2A
    const val USAGE_TAB = 0x2B
    const val USAGE_SPACE = 0x2C
    const val USAGE_RIGHT = 0x4F
    const val USAGE_LEFT = 0x50
    const val USAGE_DOWN = 0x51
    const val USAGE_UP = 0x52

    fun usageForLetter(letter: Char): Int? {
        val lower = letter.lowercaseChar()
        if (lower !in 'a'..'z') return null
        return USAGE_A + (lower - 'a')
    }

    fun usageForDigit(digit: Char): Int? {
        if (digit !in '0'..'9') return null
        return if (digit == '0') USAGE_1 + 9 else USAGE_1 + (digit - '1')
    }
}
