package app.synco.sync

import app.synco.protocol.clip.ClipHash
import app.synco.protocol.message.CapsFlags
import app.synco.protocol.message.ClipRep
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipRouterSendTest {

    private val link = RecordingPeerLink()
    private val events = RecordingEvents()
    private val blobs = StubBlobSender()

    @Test
    fun `an enabled text clip leaves as one clip envelope`() = runTest {
        val router = router(FixedPolicy())
        val snapshot = ClipFixtures.snapshot(ClipRep.Text("hello"))

        assertTrue(router.send(snapshot))

        val clip = link.clips.single()
        assertEquals(snapshot.clipId, clip.id)
        assertEquals(ClipFixtures.SELF, clip.origin)
        assertEquals(snapshot.hash, clip.hash)
        assertEquals(listOf(ClipRep.Text("hello")), clip.reps)
        assertEquals(SyncEvent.Kind.CLIP_SENT, events.last?.kind)
    }

    @Test
    fun `a clip whose only representation is disabled is not sent at all`() = runTest {
        val textDisabled = CapsFlags(text = false, image = true, file = true)
        val router = router(FixedPolicy(ClipFixtures.policy(send = textDisabled)))

        assertFalse(router.send(ClipFixtures.snapshot(ClipRep.Text("hello"))))

        assertTrue(link.envelopes.isEmpty())
        assertEquals(SyncEvent.Kind.CLIP_DROPPED, events.last?.kind)
    }

    @Test
    fun `pausing suppresses the outbound direction`() = runTest {
        val router = router(FixedPolicy(ClipFixtures.policy(paused = true)))

        assertFalse(router.send(ClipFixtures.snapshot(ClipRep.Text("hello"))))

        assertTrue(link.envelopes.isEmpty())
        assertEquals(SyncEvent.Kind.CLIP_DROPPED, events.last?.kind)
    }

    @Test
    fun `a disabled representation is stripped and the hash is recomputed`() = runTest {
        val imageDisabled = CapsFlags(text = true, image = false, file = true)
        val router = router(FixedPolicy(ClipFixtures.policy(send = imageDisabled)))
        val text = ClipRep.Text("hello")
        val image = ClipFixtures.image(ClipFixtures.transferId(1))

        assertTrue(router.send(ClipFixtures.snapshot(image, text)))

        val clip = link.clips.single()
        assertEquals(listOf(text), clip.reps)
        assertEquals(ClipHash.compute(listOf(text)), clip.hash)
        assertTrue(blobs.streamed.isEmpty())
    }

    @Test
    fun `a representation over the peer budget is stripped`() = runTest {
        val router = router(FixedPolicy(peerMaxBlobBytes = 8L))
        val text = ClipRep.Text("hello")
        val image = ClipFixtures.image(ClipFixtures.transferId(1), size = 64L)

        assertTrue(router.send(ClipFixtures.snapshot(image, text)))

        assertEquals(listOf(text), link.clips.single().reps)
        assertTrue(blobs.streamed.isEmpty())
    }

    @Test
    fun `an enabled blob representation is announced and then streamed`() = runTest {
        val router = router(FixedPolicy())
        val image = ClipFixtures.image(ClipFixtures.transferId(1))
        val snapshot = ClipFixtures.snapshot(image, ClipRep.Text("hello"))

        assertTrue(router.send(snapshot))

        assertEquals(snapshot.hash, link.clips.single().hash)
        assertEquals(listOf<ClipRep>(image), blobs.streamed)
        assertEquals(SyncEvent.Kind.CLIP_SENT, events.last?.kind)
    }

    @Test
    fun `a failed blob stream reports the clip as unsent`() = runTest {
        val router = ClipFixtures.router(
            link = link,
            settings = FixedPolicy(),
            blobs = StubBlobSender(delivers = false),
            events = events,
        )

        assertFalse(router.send(ClipFixtures.snapshot(ClipFixtures.image(ClipFixtures.transferId(1)))))

        assertEquals(SyncEvent.Kind.TRANSFER_FAILED, events.last?.kind)
    }

    private fun router(settings: PeerPolicySource): ClipRouter = ClipFixtures.router(
        link = link,
        settings = settings,
        blobs = blobs,
        events = events,
    )
}
