package app.synco.sync

import app.synco.protocol.framing.BlobChunk
import app.synco.protocol.message.AckReason
import app.synco.protocol.message.TransferAbort
import app.synco.protocol.message.TransferEnd
import app.synco.protocol.message.TransferStart
import app.synco.transfer.TransferFailure
import app.synco.transfer.TransferOutcome
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class ClipRouterTransferHoldTest {

    private val link = RecordingPeerLink()
    private val clipboard = RecordingClipboard()
    private val transfers = StubTransferGateway()
    private val router = ClipFixtures.router(
        link = link,
        settings = FixedPolicy(),
        clipboard = clipboard,
        transfers = transfers,
    )

    private val first = ClipFixtures.transferId(1)
    private val second = ClipFixtures.transferId(2)
    private val clip = ClipFixtures.clip(ClipFixtures.image(first), ClipFixtures.image(second, name = "two.png"))

    @Test
    fun `a clip waits for every announced transfer before it is applied`() = runTest {
        router.receive(clip)
        assertTrue(clipboard.writes.isEmpty())
        assertTrue(link.envelopes.isEmpty())

        deliver(first)
        assertTrue(clipboard.writes.isEmpty())
        assertTrue(link.acks.isEmpty())

        deliver(second)

        val write = clipboard.writes.single()
        assertEquals(clip.hash, write.clipHash)
        assertEquals(setOf(first.toString(), second.toString()), write.blobs.keys)
        assertEquals(clip.reps, write.reps)
        assertTrue(link.acks.single().applied)
    }

    @Test
    fun `a digest mismatch declines the whole clip and aborts what is left`() = runTest {
        transfers.completeWith(second, TransferOutcome.Failed(second, TransferFailure.HASH_MISMATCH))

        router.receive(clip)
        deliver(first)
        deliver(second)

        assertTrue(clipboard.writes.isEmpty())
        val ack = link.acks.single()
        assertFalse(ack.applied)
        assertEquals(AckReason.HASH_MISMATCH, ack.ackReason)
        assertTrue(second in transfers.aborted)
    }

    @Test
    fun `a transfer the sender could not finish declines the clip`() = runTest {
        router.receive(clip)
        router.receive(TransferStart(first.toString(), clip.id, "one.png", "image/png", 4L, DIGEST))
        router.receive(TransferEnd(first.toString(), false))

        assertTrue(clipboard.writes.isEmpty())
        assertEquals(AckReason.HASH_MISMATCH, link.acks.single().ackReason)
    }

    @Test
    fun `an abort from the peer declines the clip`() = runTest {
        router.receive(clip)
        router.receive(TransferStart(first.toString(), clip.id, "one.png", "image/png", 4L, DIGEST))
        router.receive(TransferAbort(first.toString(), AckReason.USER_CANCELLED.wireValue))

        assertTrue(clipboard.writes.isEmpty())
        assertEquals(AckReason.USER_CANCELLED, link.acks.single().ackReason)
    }

    @Test
    fun `a transfer nobody announced is aborted without touching the clipboard`() = runTest {
        val stray = ClipFixtures.transferId(9)

        router.receive(TransferStart(stray.toString(), clip.id, "stray.png", "image/png", 4L, DIGEST))

        assertEquals(listOf(stray), transfers.aborted)
        assertEquals(stray.toString(), link.aborts.single().transferId)
        assertTrue(clipboard.writes.isEmpty())
    }

    private suspend fun deliver(transferId: UUID) {
        router.receive(TransferStart(transferId.toString(), clip.id, "$transferId.png", "image/png", 4L, DIGEST))
        router.receive(BlobChunk(transferId, 0L, byteArrayOf(1, 2, 3, 4)))
        router.receive(TransferEnd(transferId.toString(), true))
    }

    private companion object {
        const val DIGEST = "5f7b3c1d5f7b3c1d5f7b3c1d5f7b3c1d5f7b3c1d5f7b3c1d5f7b3c1d5f7b3c1d"
    }
}
