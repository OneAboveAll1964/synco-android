package app.synco.transport

import app.synco.protocol.ProtocolConstants
import app.synco.protocol.SyncoError
import app.synco.protocol.message.EnvelopeCodec
import app.synco.protocol.message.Ping
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionKeepAliveTest {

    @Test
    fun `pings once the write idleness reaches the interval`() = runTest {
        val clock = MutableClock()
        val link = TestLink(clock.elapsedMillis)
        clock.advance(ProtocolConstants.PING_INTERVAL_MILLIS)

        val heartbeat = launch { SessionHeartbeat(link.left, link.left.activity).run() }
        assertEquals(Ping(0), EnvelopeCodec.decode(link.right.read().body))
        heartbeat.cancel()
    }

    @Test
    fun `waits for the whole interval before pinging`() = runTest {
        val clock = MutableClock()
        val link = TestLink(clock.elapsedMillis)
        clock.advance(ProtocolConstants.PING_INTERVAL_MILLIS - 1)

        val heartbeat = launch { SessionHeartbeat(link.left, link.left.activity).run() }
        runCurrent()
        assertTrue(heartbeat.isActive)
        clock.advance(1)
        advanceTimeBy(ProtocolConstants.PING_INTERVAL_MILLIS)
        assertEquals(Ping(0), EnvelopeCodec.decode(link.right.read().body))
        heartbeat.cancel()
    }

    @Test
    fun `read watchdog gives up after the read timeout`() = runTest {
        val clock = MutableClock()
        val activity = ConnectionActivity(clock.elapsedMillis)
        clock.advance(ProtocolConstants.READ_TIMEOUT_MILLIS)

        val failure = runCatching { ReadTimeoutWatchdog(activity).run() }.exceptionOrNull()
        assertTrue(failure is SyncoError.Timeout)
    }

    @Test
    fun `read watchdog waits while frames keep arriving`() = runTest {
        val clock = MutableClock()
        val activity = ConnectionActivity(clock.elapsedMillis)
        clock.advance(ProtocolConstants.READ_TIMEOUT_MILLIS)
        activity.recordRead()

        val watchdog = launch { ReadTimeoutWatchdog(activity).run() }
        runCurrent()
        assertTrue(watchdog.isActive)
        watchdog.cancel()
    }
}
