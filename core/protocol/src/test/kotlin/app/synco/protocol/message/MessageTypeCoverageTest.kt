package app.synco.protocol.message

import org.junit.Assert.assertEquals
import org.junit.Test

class MessageTypeCoverageTest {

    @Test
    fun `every declared message type has a fixture`() {
        val covered = MessageFixtures.ALL
            .map { EnvelopeCodec.decode(EnvelopeCodec.encodeToString(it)) }
            .mapNotNull { discriminatorOf(EnvelopeCodec.encodeToString(it)) }
            .toSet()
        assertEquals(MessageType.KNOWN, covered)
    }

    @Test
    fun `caps without the adb flag reads as false`() {
        val json = """{"t":"caps","accepts":{"text":true,"image":true,"file":true},""" +
            """"sends":{"text":true,"image":true,"file":true}}"""
        val caps = EnvelopeCodec.decode(json) as Caps
        assertEquals(false, caps.adbShizuku)
    }

    @Test
    fun `a shizuku start request carries nothing but its discriminator`() {
        assertEquals("""{"t":"shizukuStart"}""", EnvelopeCodec.encodeToString(ShizukuStartRequest))
    }

    @Test
    fun `a successful shizuku result omits the reason`() {
        assertEquals(
            """{"t":"shizukuStartResult","started":true}""",
            EnvelopeCodec.encodeToString(ShizukuStartResult(started = true)),
        )
    }

    private fun discriminatorOf(json: String): String? =
        Regex(""""t":"([^"]+)"""").find(json)?.groupValues?.get(1)
}
