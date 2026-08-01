package app.synco.transfer

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class ProgressThrottleTest {

    private val transferId: UUID = UUID.randomUUID()
    private var now = 0L
    private val throttle = ProgressThrottle(minIntervalMillis = 200) { now }

    @Test
    fun `a terminal update is never dropped`() {
        throttle.allows(transferId, TransferProgress.State.RUNNING, 10, 100)

        assertTrue(throttle.allows(transferId, TransferProgress.State.COMPLETED, 100, 100))
        assertTrue(throttle.allows(transferId, TransferProgress.State.FAILED, 100, 100))
        assertTrue(throttle.allows(transferId, TransferProgress.State.STARTED, 0, 100))
    }

    @Test
    fun `chunks at the same percent within the interval are dropped`() {
        assertTrue(throttle.allows(transferId, TransferProgress.State.RUNNING, 10, 1000))

        now += 10
        assertFalse(throttle.allows(transferId, TransferProgress.State.RUNNING, 11, 1000))
        assertFalse(throttle.allows(transferId, TransferProgress.State.RUNNING, 12, 1000))
    }

    @Test
    fun `a changed percent always gets through`() {
        assertTrue(throttle.allows(transferId, TransferProgress.State.RUNNING, 10, 1000))

        now += 5
        assertTrue(throttle.allows(transferId, TransferProgress.State.RUNNING, 250, 1000))
    }

    @Test
    fun `time passing lets an update through again`() {
        assertTrue(throttle.allows(transferId, TransferProgress.State.RUNNING, 10, 1000))

        now += 250
        assertTrue(throttle.allows(transferId, TransferProgress.State.RUNNING, 11, 1000))
    }

    @Test
    fun `each transfer is throttled on its own`() {
        val other = UUID.randomUUID()
        assertTrue(throttle.allows(transferId, TransferProgress.State.RUNNING, 10, 1000))

        assertTrue(throttle.allows(other, TransferProgress.State.RUNNING, 10, 1000))
    }

    @Test
    fun `an unknown total never divides by zero`() {
        assertTrue(throttle.allows(transferId, TransferProgress.State.RUNNING, 10, 0))
    }
}
