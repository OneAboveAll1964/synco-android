package app.synco.transport

import app.synco.protocol.ProtocolConstants
import app.synco.protocol.SyncoError
import kotlinx.coroutines.delay

internal class ReadTimeoutWatchdog(
    private val activity: ConnectionActivity,
    private val timeoutMillis: Long = ProtocolConstants.READ_TIMEOUT_MILLIS,
) {
    suspend fun run() {
        while (true) {
            val remainingMillis = timeoutMillis - activity.millisSinceRead
            if (remainingMillis <= 0) {
                throw SyncoError.Timeout("no frame arrived within ${timeoutMillis}ms")
            }
            delay(remainingMillis)
        }
    }
}
