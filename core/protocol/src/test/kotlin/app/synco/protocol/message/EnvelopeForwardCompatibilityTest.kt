package app.synco.protocol.message

import app.synco.protocol.SyncoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class EnvelopeForwardCompatibilityTest {

    @Test
    fun `decodes an unrecognised message type instead of throwing`() {
        val unknown = EnvelopeCodec.decode(FUTURE_MESSAGE) as UnknownMessage
        assertEquals("quantumClip", unknown.type)
        assertEquals(3, unknown.body.size)
    }

    @Test
    fun `re encodes an unrecognised message verbatim`() {
        assertEquals(FUTURE_MESSAGE, EnvelopeCodec.encodeToString(EnvelopeCodec.decode(FUTURE_MESSAGE)))
    }

    @Test
    fun `ignores unrecognised fields on a recognised message`() {
        assertEquals(Ping(9), EnvelopeCodec.decode("""{"t":"ping","seq":9,"jitterHint":4}"""))
    }

    @Test
    fun `ignores unrecognised fields on a clip representation`() {
        val clip = EnvelopeCodec.decode(clipJson("""{"k":"text","text":"hi","lang":"en"}""")) as Clip
        assertEquals(listOf<ClipRep>(ClipRep.Text("hi")), clip.reps)
    }

    @Test
    fun `tolerates an unrecognised close reason`() {
        val bye = EnvelopeCodec.decode("""{"t":"bye","reason":"solarFlare"}""") as Bye
        assertEquals(null, bye.closeReason)
        assertEquals("solarFlare", bye.reason)
    }

    @Test
    fun `tolerates an unrecognised ack reason`() {
        val ack = EnvelopeCodec.decode("""{"t":"ack","id":"c","applied":false,"reason":"gremlins"}""") as Ack
        assertEquals(null, ack.ackReason)
        assertEquals("gremlins", ack.reason)
    }

    @Test
    fun `rejects a frame that is not a json object`() {
        assertThrows(SyncoError.Malformed::class.java) { EnvelopeCodec.decode("[]") }
        assertThrows(SyncoError.Malformed::class.java) { EnvelopeCodec.decode("not json") }
    }

    @Test
    fun `rejects a frame without a discriminator`() {
        assertThrows(SyncoError.Malformed::class.java) { EnvelopeCodec.decode("""{"seq":1}""") }
        assertThrows(SyncoError.Malformed::class.java) { EnvelopeCodec.decode("""{"t":7}""") }
    }

    @Test
    fun `rejects a recognised message whose fields do not match the spec`() {
        assertThrows(SyncoError.Malformed::class.java) {
            EnvelopeCodec.decode("""{"t":"ping","seq":"soon"}""")
        }
        assertThrows(SyncoError.Malformed::class.java) {
            EnvelopeCodec.decode("""{"t":"transferEnd","transferId":"t-1"}""")
        }
        assertThrows(SyncoError.Malformed::class.java) {
            EnvelopeCodec.decode("""{"t":"hello","v":1,"did":"x","dn":"n","pl":"linux","ePub":"AA=="}""")
        }
    }

    @Test
    fun `rejects a clip representation of an unrecognised kind`() {
        assertThrows(SyncoError.Malformed::class.java) {
            EnvelopeCodec.decode(clipJson("""{"k":"hologram","frames":3}"""))
        }
    }

    private fun clipJson(rep: String): String =
        """{"t":"clip","id":"c","ts":1,"origin":"$DEVICE_ID","hash":"h","reps":[$rep]}"""

    private companion object {
        const val DEVICE_ID = "abcdefghij234567"
        const val FUTURE_MESSAGE = """{"t":"quantumClip","payload":{"a":1},"n":7}"""
    }
}
