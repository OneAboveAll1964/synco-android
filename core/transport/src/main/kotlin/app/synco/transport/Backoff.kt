package app.synco.transport

import app.synco.protocol.DiscoveryConstants
import kotlinx.coroutines.delay
import kotlin.random.Random

class Backoff(
    private val baseDelayMillis: Long = DiscoveryConstants.RECONNECT_BASE_DELAY_MILLIS,
    private val factor: Long = DiscoveryConstants.RECONNECT_FACTOR,
    private val capMillis: Long = DiscoveryConstants.RECONNECT_CAP_MILLIS,
    private val random: Random = Random.Default,
) {
    private var attempt = 0

    val attempts: Int get() = attempt

    fun reset() {
        attempt = 0
    }

    fun ceilingMillis(): Long {
        var ceiling = baseDelayMillis
        repeat(attempt) {
            if (ceiling >= capMillis / factor) return capMillis
            ceiling *= factor
        }
        return minOf(ceiling, capMillis)
    }

    fun nextDelayMillis(): Long {
        val ceiling = ceilingMillis()
        attempt++
        return random.nextLong(ceiling + 1)
    }

    suspend fun awaitNextDelay(): Long = nextDelayMillis().also { delay(it) }
}
