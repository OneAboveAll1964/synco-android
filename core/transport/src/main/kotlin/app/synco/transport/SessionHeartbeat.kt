package app.synco.transport

import app.synco.protocol.ProtocolConstants
import app.synco.protocol.message.Ping
import kotlinx.coroutines.delay

internal class SessionHeartbeat(
    private val frames: FramedConnection,
    private val activity: ConnectionActivity,
    private val intervalMillis: Long = ProtocolConstants.PING_INTERVAL_MILLIS,
) {
    private var sequence = 0L

    suspend fun run() {
        while (true) {
            val remainingMillis = intervalMillis - activity.millisSinceWrite
            if (remainingMillis > 0) {
                delay(remainingMillis)
            } else {
                frames.write(Ping(sequence++))
            }
        }
    }
}
