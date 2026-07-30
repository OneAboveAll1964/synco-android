package app.synco.clipboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CopyGateThrottleTest {

    @Test
    fun firstRequestOpensTheGate() {
        assertTrue(CopyGateThrottle(minIntervalMillis = 500L).accept(nowMillis = 0))
    }

    @Test
    fun secondRequestWithinIntervalIsRejected() {
        val throttle = CopyGateThrottle(minIntervalMillis = 500L)
        assertTrue(throttle.accept(nowMillis = 0))
        assertFalse(throttle.accept(nowMillis = 200))
    }

    @Test
    fun requestAtOrAfterIntervalIsAccepted() {
        val throttle = CopyGateThrottle(minIntervalMillis = 500L)
        assertTrue(throttle.accept(nowMillis = 0))
        assertTrue(throttle.accept(nowMillis = 500))
    }

    @Test
    fun burstCollapsesToSingleOpen() {
        val throttle = CopyGateThrottle(minIntervalMillis = 500L)
        val accepted = listOf(0L, 50L, 120L, 300L, 499L).count { throttle.accept(it) }
        assertTrue(accepted == 1)
    }

    @Test
    fun spacedRequestsEachOpen() {
        val throttle = CopyGateThrottle(minIntervalMillis = 500L)
        val accepted = listOf(0L, 600L, 1_200L).count { throttle.accept(it) }
        assertTrue(accepted == 3)
    }
}
