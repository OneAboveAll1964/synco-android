package app.synco.remote

import app.synco.protocol.message.RemoteInputEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackpadTranslatorTest {

    private fun translator(pointer: RemotePointer = RemotePointer()) =
        TrackpadTranslator(viewWidth = 1000.0, viewHeight = 1000.0, pointer = pointer, moveScale = 1.0)

    private fun touch(x: Double, y: Double, id: Int = 0) = Touch(id, x, y)

    @Test
    fun `a single finger drag moves the pointer proportionally`() {
        val pointer = RemotePointer()
        val trackpad = translator(pointer)

        trackpad.onTouch(TouchPhase.START, listOf(touch(500.0, 500.0)), 0)
        val events = trackpad.onTouch(TouchPhase.MOVE, listOf(touch(600.0, 500.0)), 30)

        assertEquals(1, events.size)
        assertEquals(RemoteInputEvent.POINTER_ABSOLUTE, events.first().kind)
        assertEquals(0.6, events.first().x!!, 1e-9)
        assertEquals(0.5, events.first().y!!, 1e-9)
    }

    @Test
    fun `a quick one finger tap is a left click`() {
        val trackpad = translator()

        trackpad.onTouch(TouchPhase.START, listOf(touch(500.0, 500.0)), 0)
        val events = trackpad.onTouch(TouchPhase.END, emptyList(), 100)

        assertEquals(listOf(RemoteButtons.LEFT, RemoteButtons.LEFT), events.filter { it.kind == RemoteInputEvent.BUTTON }.map { it.button })
        assertEquals(listOf(true, false), events.filter { it.kind == RemoteInputEvent.BUTTON }.map { it.down })
    }

    @Test
    fun `a quick two finger tap is a right click`() {
        val trackpad = translator()

        trackpad.onTouch(TouchPhase.START, listOf(touch(400.0, 500.0, 0)), 0)
        trackpad.onTouch(TouchPhase.START, listOf(touch(400.0, 500.0, 0), touch(500.0, 500.0, 1)), 10)
        val events = trackpad.onTouch(TouchPhase.END, emptyList(), 120)

        assertTrue(events.any { it.kind == RemoteInputEvent.BUTTON && it.button == RemoteButtons.RIGHT })
    }

    @Test
    fun `a slow press is not a tap`() {
        val trackpad = translator()

        trackpad.onTouch(TouchPhase.START, listOf(touch(500.0, 500.0)), 0)
        val events = trackpad.onTouch(TouchPhase.END, emptyList(), 500)

        assertTrue(events.none { it.kind == RemoteInputEvent.BUTTON })
    }

    @Test
    fun `a moved finger does not tap on release`() {
        val trackpad = translator()

        trackpad.onTouch(TouchPhase.START, listOf(touch(500.0, 500.0)), 0)
        trackpad.onTouch(TouchPhase.MOVE, listOf(touch(560.0, 560.0)), 30)
        val events = trackpad.onTouch(TouchPhase.END, emptyList(), 100)

        assertTrue(events.none { it.kind == RemoteInputEvent.BUTTON })
    }

    @Test
    fun `two fingers moving together scroll`() {
        val trackpad = translator()

        trackpad.onTouch(TouchPhase.START, listOf(touch(400.0, 500.0, 0)), 0)
        trackpad.onTouch(TouchPhase.START, listOf(touch(400.0, 500.0, 0), touch(500.0, 500.0, 1)), 10)
        val events = trackpad.onTouch(
            TouchPhase.MOVE,
            listOf(touch(400.0, 560.0, 0), touch(500.0, 560.0, 1)),
            40,
        )

        val scroll = events.single { it.kind == RemoteInputEvent.SCROLL }
        assertEquals(60.0, scroll.dy!!, 1e-9)
        assertEquals(0.0, scroll.dx!!, 1e-9)
    }

    @Test
    fun `two fingers spreading apart magnify`() {
        val trackpad = translator()

        trackpad.onTouch(TouchPhase.START, listOf(touch(450.0, 500.0, 0)), 0)
        trackpad.onTouch(TouchPhase.START, listOf(touch(450.0, 500.0, 0), touch(550.0, 500.0, 1)), 10)
        val events = trackpad.onTouch(
            TouchPhase.MOVE,
            listOf(touch(400.0, 500.0, 0), touch(600.0, 500.0, 1)),
            40,
        )

        val magnify = events.single { it.kind == RemoteInputEvent.MAGNIFY }
        assertEquals(2.0, magnify.scale!!, 1e-9)
    }

    @Test
    fun `a cancel drops any pending tap`() {
        val trackpad = translator()

        trackpad.onTouch(TouchPhase.START, listOf(touch(500.0, 500.0)), 0)
        val cancelled = trackpad.onTouch(TouchPhase.CANCEL, emptyList(), 50)
        val afterEnd = trackpad.onTouch(TouchPhase.END, emptyList(), 60)

        assertTrue(cancelled.isEmpty())
        assertTrue(afterEnd.isEmpty())
    }
}
