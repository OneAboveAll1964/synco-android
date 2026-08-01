package app.synco.sync

import app.synco.transfer.TransferProgress
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class StuckTransferTest {

    private val transferId: UUID = UUID.randomUUID()

    @Test
    fun `a running transfer stays on the list`() = runTest {
        val holder = SyncStateHolder()

        holder.recordTransfer(progress(TransferProgress.State.RUNNING))

        assertEquals(listOf(transferId), holder.state.value.transfers.map { it.transferId })
    }

    @Test
    fun `a failed transfer leaves the list`() = runTest {
        val holder = SyncStateHolder()
        holder.recordTransfer(progress(TransferProgress.State.RUNNING))

        holder.recordTransfer(progress(TransferProgress.State.FAILED))

        assertTrue(holder.state.value.transfers.isEmpty())
    }

    @Test
    fun `cancelling clears a row the transfer manager no longer knows about`() = runTest {
        val holder = SyncStateHolder()
        holder.recordTransfer(progress(TransferProgress.State.RUNNING))

        holder.dropTransfer(transferId)

        assertTrue(holder.state.value.transfers.isEmpty())
    }

    @Test
    fun `a row whose updates stopped is reaped`() = runTest {
        val holder = SyncStateHolder()
        holder.recordTransfer(progress(TransferProgress.State.RUNNING), atMillis = 1_000)

        holder.reapStaleTransfers(liveIds = setOf(transferId), atMillis = 1_000 + 20_001)

        assertTrue(holder.state.value.transfers.isEmpty())
    }

    @Test
    fun `a row that is still being updated survives reaping`() = runTest {
        val holder = SyncStateHolder()
        holder.recordTransfer(progress(TransferProgress.State.RUNNING), atMillis = 1_000)

        holder.reapStaleTransfers(liveIds = setOf(transferId), atMillis = 1_000 + 5_000)

        assertEquals(listOf(transferId), holder.state.value.transfers.map { it.transferId })
    }

    @Test
    fun `a new transfer sweeps away an abandoned one`() = runTest {
        val holder = SyncStateHolder()
        holder.recordTransfer(progress(TransferProgress.State.RUNNING), atMillis = 1_000)

        holder.recordTransfer(
            progress(TransferProgress.State.RUNNING).copy(transferId = UUID.randomUUID()),
            atMillis = 1_000 + 20_001,
        )

        assertEquals(1, holder.state.value.transfers.size)
    }

    @Test
    fun `a row for a transfer the manager forgot is swept away`() = runTest {
        val holder = SyncStateHolder()
        holder.recordTransfer(progress(TransferProgress.State.RUNNING), atMillis = 1_000)

        holder.reapStaleTransfers(liveIds = emptySet(), atMillis = 1_100)

        assertTrue(holder.state.value.transfers.isEmpty())
    }

    @Test
    fun `dropping an unknown transfer leaves the others alone`() = runTest {
        val holder = SyncStateHolder()
        holder.recordTransfer(progress(TransferProgress.State.RUNNING))

        holder.dropTransfer(UUID.randomUUID())

        assertEquals(listOf(transferId), holder.state.value.transfers.map { it.transferId })
    }

    private fun progress(state: TransferProgress.State): TransferProgress = TransferProgress(
        transferId = transferId,
        direction = TransferProgress.Direction.OUTGOING,
        state = state,
        name = "holiday.mp4",
        bytesTransferred = 512,
        totalBytes = 4096,
    )
}
