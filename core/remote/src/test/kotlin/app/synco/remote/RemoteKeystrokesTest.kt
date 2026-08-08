package app.synco.remote

import app.synco.protocol.message.RemoteInputEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteKeystrokesTest {

    @Test
    fun `typing appends text`() {
        val events = RemoteKeystrokes.edits("ab", "abc")
        assertEquals(1, events.size)
        assertEquals(RemoteInputEvent.TEXT, events.first().kind)
        assertEquals("c", events.first().text)
    }

    @Test
    fun `deleting sends one backspace per character`() {
        val events = RemoteKeystrokes.edits("abcd", "ab")
        assertEquals(4, events.size)
        assertEquals(HidKeyboard.USAGE_BACKSPACE, events.first().code)
        assertEquals(true, events.first().down)
        assertEquals(false, events[1].down)
    }

    @Test
    fun `replacing deletes then types`() {
        val events = RemoteKeystrokes.edits("cat", "cow")
        assertEquals(HidKeyboard.USAGE_BACKSPACE, events[0].code)
        assertEquals(HidKeyboard.USAGE_BACKSPACE, events[2].code)
        assertEquals("ow", events.last().text)
    }

    @Test
    fun `newlines become enter keys`() {
        val events = RemoteKeystrokes.edits("", "hi\nthere")
        assertEquals("hi", events[0].text)
        assertEquals(HidKeyboard.USAGE_ENTER, events[1].code)
        assertEquals("there", events[3].text)
    }

    @Test
    fun `modifiers turn characters into key presses`() {
        val events = RemoteKeystrokes.edits("", "c", RemoteModifiers.META)
        assertEquals(2, events.size)
        assertEquals(HidKeyboard.usageForLetter('c'), events.first().code)
        assertEquals(RemoteModifiers.META, events.first().mods)
    }

    @Test
    fun `unchanged text emits nothing`() {
        assertEquals(emptyList<RemoteInputEvent>(), RemoteKeystrokes.edits("same", "same"))
    }
}
