package app.synco.shizuku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShizukuUriFactsTest {

    @Test
    fun `a real row gives back the name, size and mime`() {
        val facts = ShizukuUriFacts.parse(
            "Row: 0 _display_name=holiday.jpg, _size=204800, mime_type=image/jpeg\n",
        )

        assertEquals("holiday.jpg", facts?.name)
        assertEquals(204_800L, facts?.size)
        assertEquals("image/jpeg", facts?.mime)
    }

    @Test
    fun `a name containing spaces survives`() {
        val facts = ShizukuUriFacts.parse(
            "Row: 0 _display_name=my holiday photo.png, _size=12, mime_type=image/png",
        )

        assertEquals("my holiday photo.png", facts?.name)
    }

    @Test
    fun `null columns are treated as missing`() {
        val facts = ShizukuUriFacts.parse(
            "Row: 0 _display_name=NULL, _size=NULL, mime_type=video/mp4",
        )

        assertNull(facts?.name)
        assertNull(facts?.size)
        assertEquals("video/mp4", facts?.mime)
    }

    @Test
    fun `output with no row gives nothing`() {
        assertNull(ShizukuUriFacts.parse("No result found."))
        assertNull(ShizukuUriFacts.parse(""))
    }

    @Test
    fun `an entirely empty row gives nothing`() {
        assertNull(ShizukuUriFacts.parse("Row: 0 _display_name=NULL, _size=NULL, mime_type=NULL"))
    }
}
