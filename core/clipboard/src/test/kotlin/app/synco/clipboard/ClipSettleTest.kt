package app.synco.clipboard

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ClipSettleTest {

    @Test
    fun firstReadingIsAlwaysFresh() = runBlocking {
        val settle = ClipSettle(budgetMillis = { 120L }, stepMillis = 10L)
        assertEquals(SettleOutcome.FRESH, settle.awaitFresh { 1_000L })
    }

    @Test
    fun anUnchangedTimestampSettlesStale() = runBlocking {
        val settle = ClipSettle(budgetMillis = { 60L }, stepMillis = 10L)
        settle.awaitFresh { 1_000L }
        assertEquals(SettleOutcome.STALE, settle.awaitFresh { 1_000L })
    }

    @Test
    fun aTimestampThatAdvancesWhileWaitingIsFresh() = runBlocking {
        val settle = ClipSettle(budgetMillis = { 300L }, stepMillis = 10L)
        settle.awaitFresh { 1_000L }
        var reads = 0
        val outcome = settle.awaitFresh {
            reads += 1
            if (reads < 4) 1_000L else 2_000L
        }
        assertEquals(SettleOutcome.FRESH, outcome)
    }

    @Test
    fun anEarlierTimestampNeverCountsAsFresh() = runBlocking {
        val settle = ClipSettle(budgetMillis = { 60L }, stepMillis = 10L)
        settle.awaitFresh { 2_000L }
        assertEquals(SettleOutcome.STALE, settle.awaitFresh { 1_000L })
    }

    @Test
    fun anUnreadableClipboardIsReportedRatherThanWaitedOut() = runBlocking {
        val settle = ClipSettle(budgetMillis = { 5_000L }, stepMillis = 10L)
        assertEquals(SettleOutcome.UNAVAILABLE, settle.awaitFresh { null })
    }
}
