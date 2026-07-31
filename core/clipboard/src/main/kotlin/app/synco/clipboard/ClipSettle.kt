package app.synco.clipboard

import kotlinx.coroutines.delay

class ClipSettle(
    private val budgetMillis: () -> Long = { DEFAULT_BUDGET_MILLIS },
    private val stepMillis: Long = DEFAULT_STEP_MILLIS,
) {
    private var lastTimestamp = UNSET

    suspend fun awaitFresh(timestamp: () -> Long?): SettleOutcome {
        val first = timestamp() ?: return SettleOutcome.UNAVAILABLE
        if (advancedTo(first)) return SettleOutcome.FRESH
        val budget = budgetMillis()
        var waited = 0L
        while (waited < budget) {
            delay(stepMillis)
            waited += stepMillis
            val current = timestamp() ?: return SettleOutcome.UNAVAILABLE
            if (advancedTo(current)) return SettleOutcome.FRESH
        }
        return SettleOutcome.STALE
    }

    private fun advancedTo(candidate: Long): Boolean {
        if (lastTimestamp != UNSET && candidate <= lastTimestamp) return false
        lastTimestamp = candidate
        return true
    }

    companion object {
        const val DEFAULT_BUDGET_MILLIS = 200L
        const val DEFAULT_STEP_MILLIS = 20L
        private const val UNSET = Long.MIN_VALUE
    }
}
