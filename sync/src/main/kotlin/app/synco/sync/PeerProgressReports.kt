package app.synco.sync

import java.util.UUID

internal class PeerProgressReports(
    private val minIntervalMillis: Long = DEFAULT_MIN_INTERVAL_MILLIS,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val lastSentAt = mutableMapOf<UUID, Long>()

    fun shouldReport(transferId: UUID): Boolean {
        val now = clock()
        val last = lastSentAt[transferId]
        if (last != null && now - last < minIntervalMillis) return false
        lastSentAt[transferId] = now
        return true
    }

    fun forget(transferId: UUID) {
        lastSentAt.remove(transferId)
    }

    private companion object {
        const val DEFAULT_MIN_INTERVAL_MILLIS = 500L
    }
}
