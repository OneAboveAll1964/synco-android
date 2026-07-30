package app.synco.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

internal class NsdResolver(private val nsdManager: NsdManager) {

    private val serialisation = Mutex()

    suspend fun resolve(request: NsdServiceInfo): NsdServiceInfo? = serialisation.withLock {
        repeat(DiscoveryTuning.RESOLVE_ATTEMPTS) { attempt ->
            if (attempt > 0) delay(DiscoveryTuning.RESOLVE_RETRY_DELAY_MILLIS)
            attemptResolve(request)?.let { return@withLock it }
        }
        null
    }

    @Suppress("DEPRECATION")
    private suspend fun attemptResolve(request: NsdServiceInfo): NsdServiceInfo? {
        val listener = NsdResolveListener()
        nsdManager.resolveService(request, listener)
        return withTimeoutOrNull(DiscoveryTuning.RESOLVE_TIMEOUT_MILLIS) {
            runCatching { listener.await() }.getOrNull()
        }
    }
}
