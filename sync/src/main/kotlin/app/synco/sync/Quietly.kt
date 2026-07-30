package app.synco.sync

import kotlinx.coroutines.CancellationException

internal suspend fun quietly(block: suspend () -> Unit) {
    try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Exception) {
        Unit
    }
}
