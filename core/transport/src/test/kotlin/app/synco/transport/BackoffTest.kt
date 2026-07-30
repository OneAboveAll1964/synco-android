package app.synco.transport

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BackoffTest {

    @Test
    fun `doubles the ceiling from the base up to the cap`() {
        val backoff = Backoff()
        val ceilings = mutableListOf<Long>()
        repeat(8) {
            ceilings += backoff.ceilingMillis()
            backoff.nextDelayMillis()
        }
        assertEquals(
            listOf(1_000L, 2_000L, 4_000L, 8_000L, 16_000L, 30_000L, 30_000L, 30_000L),
            ceilings,
        )
    }

    @Test
    fun `keeps every jittered delay inside the current ceiling`() {
        val backoff = Backoff(random = Random(seed = 20_260_730))
        repeat(200) {
            val ceiling = backoff.ceilingMillis()
            val delay = backoff.nextDelayMillis()
            assertTrue("$delay exceeds $ceiling", delay in 0L..ceiling)
        }
        assertEquals(200, backoff.attempts)
    }

    @Test
    fun `spreads the delays with full jitter`() {
        val backoff = Backoff(random = Random(seed = 7))
        repeat(10) { backoff.nextDelayMillis() }
        val delays = List(50) { backoff.nextDelayMillis() }
        assertTrue(delays.distinct().size > 1)
        assertTrue(delays.all { it <= 30_000L })
    }

    @Test
    fun `reset returns to the base ceiling`() {
        val backoff = Backoff()
        repeat(5) { backoff.nextDelayMillis() }
        backoff.reset()
        assertEquals(0, backoff.attempts)
        assertEquals(1_000L, backoff.ceilingMillis())
    }

    @Test
    fun `awaiting a delay suspends for exactly that delay`() = runTest {
        val backoff = Backoff(random = Random(seed = 3))
        val startedAt = testScheduler.currentTime
        val slept = backoff.awaitNextDelay()
        assertEquals(slept, testScheduler.currentTime - startedAt)
    }
}
