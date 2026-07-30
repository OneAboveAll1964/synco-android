package app.synco.protocol.message

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnvelopeCodecTest {

    @Test
    fun `round trips every control message`() {
        MessageFixtures.ALL.forEach { message ->
            assertEquals(message, EnvelopeCodec.decode(EnvelopeCodec.encodeToString(message)))
        }
    }

    @Test
    fun `round trips every control message through bytes`() {
        MessageFixtures.ALL.forEach { message ->
            assertEquals(message, EnvelopeCodec.decode(EnvelopeCodec.encodeToBytes(message)))
        }
    }

    @Test
    fun `writes the spec discriminator for every control message`() {
        MessageFixtures.ALL.forEach { message ->
            val json = EnvelopeCodec.encodeToString(message)
            assertTrue(json, json.startsWith("{\"${MessageType.DISCRIMINATOR}\":\""))
        }
    }

    @Test
    fun `writes hello exactly as the spec names its fields`() {
        val json = EnvelopeCodec.encodeToString(MessageFixtures.HELLO)
        listOf(
            "\"t\":\"hello\"",
            "\"v\":1",
            "\"did\":\"${MessageFixtures.DEVICE_ID}\"",
            "\"dn\":\"Pixel\"",
            "\"pl\":\"android\"",
            "\"ePub\":\"${MessageFixtures.PUBLIC_KEY}\"",
        ).forEach { fragment -> assertTrue(json, json.contains(fragment)) }
    }

    @Test
    fun `writes a pair request with the grouped fingerprint`() {
        val json = EnvelopeCodec.encodeToString(MessageFixtures.ALL.first { it is PairRequest })
        assertTrue(json, json.contains("\"fp\":\"A1B2-C3D4-E5F6-0718\""))
        assertTrue(json, json.contains("\"sPub\":\"${MessageFixtures.PUBLIC_KEY}\""))
    }

    @Test
    fun `writes clip representations in order under the k discriminator`() {
        val json = EnvelopeCodec.encodeToString(MessageFixtures.CLIP)
        val order = listOf("html", "text", "rtf", "url", "image", "file")
            .map { kind -> json.indexOf("\"${ClipRepKind.DISCRIMINATOR}\":\"$kind\"") }
        assertEquals(order.sorted(), order)
        assertTrue(json, order.none { it < 0 })
    }

    @Test
    fun `omits null optional fields`() {
        val json = EnvelopeCodec.encodeToString(Ack.applied("clip-1"))
        assertEquals("""{"t":"ack","id":"clip-1","applied":true}""", json)
    }

    @Test
    fun `maps close and ack reasons through their wire values`() {
        assertEquals(
            """{"t":"bye","reason":"frameTooLarge"}""",
            EnvelopeCodec.encodeToString(Bye.of(CloseReason.FRAME_TOO_LARGE)),
        )
        assertEquals(
            CloseReason.FRAME_TOO_LARGE,
            (EnvelopeCodec.decode("""{"t":"bye","reason":"frameTooLarge"}""") as Bye).closeReason,
        )
        assertEquals(AckReason.TOO_LARGE, Ack.rejected("clip-1", AckReason.TOO_LARGE).ackReason)
        assertEquals(AckReason.HASH_MISMATCH, TransferAbort.of("t-1", AckReason.HASH_MISMATCH).ackReason)
    }
}
