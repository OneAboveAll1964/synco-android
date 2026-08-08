package app.synco.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class RemotePointerTest {

    @Test
    fun `it starts centred and clamps to the unit square`() {
        val pointer = RemotePointer()
        assertEquals(0.5, pointer.x, 0.0)
        assertEquals(0.5, pointer.y, 0.0)

        pointer.moveBy(1.0, -1.0)
        assertEquals(1.0, pointer.x, 0.0)
        assertEquals(0.0, pointer.y, 0.0)

        pointer.moveBy(-2.0, 2.0)
        assertEquals(0.0, pointer.x, 0.0)
        assertEquals(1.0, pointer.y, 0.0)
    }

    @Test
    fun `moveTo clamps out-of-range targets`() {
        val pointer = RemotePointer()
        pointer.moveTo(1.4, -0.3)
        assertEquals(1.0, pointer.x, 0.0)
        assertEquals(0.0, pointer.y, 0.0)
    }
}
