package app.synco.storage

import app.synco.protocol.message.AckReason
import app.synco.protocol.message.CapsFlags
import app.synco.protocol.message.ClipRep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncPolicyTest {

    private val text = ClipRep.Text("hello")
    private val html = ClipRep.Html("<b>hello</b>")
    private val image = ClipRep.Image(
        mime = "image/png",
        name = "shot.png",
        size = 91_234,
        sha256 = "ab".repeat(32),
        transferId = "3f2a0000-0000-0000-0000-000000000000",
    )

    @Test
    fun `sends every representation by default`() {
        assertEquals(listOf(text, html, image), SyncPolicy.DEFAULT.sendableReps(listOf(text, html, image)))
    }

    @Test
    fun `strips a representation whose send toggle is off`() {
        val policy = SyncPolicy.DEFAULT.copy(
            directions = PeerDirections(
                send = CapsFlags(text = true, image = false, file = false),
                receive = CapsFlags.ALL_ENABLED,
            ),
        )

        assertEquals(listOf(text, html), policy.sendableReps(listOf(text, html, image)))
    }

    @Test
    fun `treats markup and links as text`() {
        assertEquals(ClipCategory.TEXT, ClipCategory.of(html))
        assertEquals(ClipCategory.TEXT, ClipCategory.of(ClipRep.Url("https://synco.app")))
    }

    @Test
    fun `reports a disabled receive type`() {
        val policy = SyncPolicy.DEFAULT.copy(
            directions = PeerDirections(
                send = CapsFlags.ALL_ENABLED,
                receive = CapsFlags(text = true, image = false, file = false),
            ),
        )

        assertEquals(AckReason.TYPE_DISABLED, policy.rejectionFor(image))
        assertNull(policy.rejectionFor(text))
    }

    @Test
    fun `reports a blob over the cap`() {
        val policy = SyncPolicy.DEFAULT.copy(maxBlobBytes = 1_024)

        assertEquals(AckReason.TOO_LARGE, policy.rejectionFor(image))
        assertFalse(policy.maySend(image))
        assertTrue(policy.maySend(text))
    }

    @Test
    fun `honours the budget the peer advertised`() {
        assertFalse(SyncPolicy.DEFAULT.maySend(image, peerMaxBlobBytes = 1_024))
        assertEquals(
            listOf(text),
            SyncPolicy.DEFAULT.sendableReps(listOf(text, image), peerMaxBlobBytes = 1_024),
        )
    }

    @Test
    fun `pausing suppresses both directions without losing the toggles`() {
        val policy = SyncPolicy.DEFAULT.copy(paused = true)

        assertTrue(policy.send.allDisabled)
        assertTrue(policy.receive.allDisabled)
        assertEquals(AckReason.RECEIVE_DISABLED, policy.rejectionFor(text))
        assertEquals(CapsFlags.ALL_ENABLED, policy.directions.send)
    }

    @Test
    fun `advertises the effective flags`() {
        val caps = SyncPolicy.DEFAULT.copy(
            directions = PeerDirections(
                send = CapsFlags.ALL_ENABLED,
                receive = CapsFlags.ALL_DISABLED,
            ),
        ).toCaps()

        assertEquals(CapsFlags.ALL_ENABLED, caps.sends)
        assertEquals(CapsFlags.ALL_DISABLED, caps.accepts)
        assertEquals(SyncPolicy.DEFAULT.maxBlobBytes, caps.maxBlobBytes)
    }
}
