package app.synco.sync

import app.synco.protocol.message.AckReason
import app.synco.protocol.message.CapsFlags
import app.synco.protocol.message.ClipRep
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipRouterReceiveTest {

    private val link = RecordingPeerLink()
    private val clipboard = RecordingClipboard()
    private val events = RecordingEvents()

    @Test
    fun `an accepted inline clip lands on the clipboard and is acknowledged`() = runTest {
        val router = router(FixedPolicy())
        val clip = ClipFixtures.clip(ClipRep.Html("<b>hi</b>"), ClipRep.Text("hi"))

        router.receive(clip)

        val write = clipboard.writes.single()
        assertEquals(clip.hash, write.clipHash)
        assertEquals(clip.reps, write.reps)
        assertTrue(write.blobs.isEmpty())
        val ack = link.acks.single()
        assertTrue(ack.applied)
        assertEquals(clip.id, ack.clipId)
        assertEquals(SyncEvent.Kind.CLIP_APPLIED, events.last?.kind)
    }

    @Test
    fun `a clip whose types are all disabled is declined as typeDisabled`() = runTest {
        val textDisabled = CapsFlags(text = false, image = true, file = true)
        val router = router(FixedPolicy(ClipFixtures.policy(receive = textDisabled)))

        router.receive(ClipFixtures.clip(ClipRep.Text("hi")))

        val ack = link.acks.single()
        assertFalse(ack.applied)
        assertEquals(AckReason.TYPE_DISABLED, ack.ackReason)
        assertTrue(clipboard.writes.isEmpty())
    }

    @Test
    fun `pausing declines inbound clips as receiveDisabled`() = runTest {
        val router = router(FixedPolicy(ClipFixtures.policy(paused = true)))

        router.receive(ClipFixtures.clip(ClipRep.Text("hi")))

        assertEquals(AckReason.RECEIVE_DISABLED, link.acks.single().ackReason)
        assertTrue(clipboard.writes.isEmpty())
    }

    @Test
    fun `a blob over the local budget is declined as tooLarge`() = runTest {
        val router = router(FixedPolicy(ClipFixtures.policy(maxBlobBytes = 16L)))
        val image = ClipFixtures.image(ClipFixtures.transferId(1), size = 64L)

        router.receive(ClipFixtures.clip(image))

        assertEquals(AckReason.TOO_LARGE, link.acks.single().ackReason)
        assertTrue(clipboard.writes.isEmpty())
    }

    @Test
    fun `a partly disabled clip applies only the surviving representations`() = runTest {
        val imageDisabled = CapsFlags(text = true, image = false, file = true)
        val router = router(FixedPolicy(ClipFixtures.policy(receive = imageDisabled)))
        val text = ClipRep.Text("hi")
        val clip = ClipFixtures.clip(ClipFixtures.image(ClipFixtures.transferId(1)), text)

        router.receive(clip)

        assertEquals(listOf(text), clipboard.writes.single().reps)
        assertEquals(clip.hash, clipboard.writes.single().clipHash)
        assertTrue(link.acks.single().applied)
    }

    @Test
    fun `a clip that originated here is never applied`() = runTest {
        val router = router(FixedPolicy())

        router.receive(ClipFixtures.clip(ClipRep.Text("hi"), origin = ClipFixtures.SELF))

        assertTrue(clipboard.writes.isEmpty())
        assertTrue(link.envelopes.isEmpty())
    }

    @Test
    fun `a refused clipboard write is reported as userCancelled`() = runTest {
        val router = ClipFixtures.router(
            link = link,
            settings = FixedPolicy(),
            clipboard = RecordingClipboard(accepts = false),
            events = events,
        )

        router.receive(ClipFixtures.clip(ClipRep.Text("hi")))

        assertEquals(AckReason.USER_CANCELLED, link.acks.single().ackReason)
    }

    private fun router(settings: PeerPolicySource): ClipRouter = ClipFixtures.router(
        link = link,
        settings = settings,
        clipboard = clipboard,
        events = events,
    )
}
