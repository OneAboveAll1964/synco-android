package app.synco.transfer

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal class ProgressThrottle(
    private val minIntervalMillis: Long = DEFAULT_INTERVAL_MILLIS,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val lastReportAtMillis = ConcurrentHashMap<UUID, Long>()
    private val lastPercent = ConcurrentHashMap<UUID, Int>()

    fun allows(
        transferId: UUID,
        state: TransferProgress.State,
        bytesTransferred: Long,
        totalBytes: Long,
    ): Boolean {
        if (state != TransferProgress.State.RUNNING) {
            forget(transferId)
            return true
        }
        val now = clock()
        val percent = percentOf(bytesTransferred, totalBytes)
        val previous = lastReportAtMillis[transferId]
        val soonEnough = previous != null && now - previous < minIntervalMillis
        if (soonEnough && lastPercent[transferId] == percent) return false
        lastReportAtMillis[transferId] = now
        lastPercent[transferId] = percent
        return true
    }

    private fun forget(transferId: UUID) {
        lastReportAtMillis.remove(transferId)
        lastPercent.remove(transferId)
    }

    private fun percentOf(bytesTransferred: Long, totalBytes: Long): Int =
        if (totalBytes <= 0) 0 else ((bytesTransferred * 100) / totalBytes).toInt().coerceIn(0, 100)

    private companion object {
        const val DEFAULT_INTERVAL_MILLIS = 200L
    }
}
