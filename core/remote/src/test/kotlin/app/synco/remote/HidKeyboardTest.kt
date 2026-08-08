package app.synco.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HidKeyboardTest {

    @Test
    fun `letters map onto the contiguous HID range`() {
        assertEquals(0x04, HidKeyboard.usageForLetter('a'))
        assertEquals(0x04, HidKeyboard.usageForLetter('A'))
        assertEquals(0x1D, HidKeyboard.usageForLetter('z'))
        assertNull(HidKeyboard.usageForLetter('7'))
    }

    @Test
    fun `digits map with one wrapping to the end`() {
        assertEquals(0x1E, HidKeyboard.usageForDigit('1'))
        assertEquals(0x26, HidKeyboard.usageForDigit('9'))
        assertEquals(0x27, HidKeyboard.usageForDigit('0'))
        assertNull(HidKeyboard.usageForDigit('a'))
    }
}
