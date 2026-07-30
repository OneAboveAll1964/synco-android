package app.synco.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.CompletableDeferred

internal class NsdResolveListener : NsdManager.ResolveListener {

    private val resolved = CompletableDeferred<NsdServiceInfo>()

    suspend fun await(): NsdServiceInfo = resolved.await()

    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
        resolved.complete(serviceInfo)
    }

    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
        resolved.completeExceptionally(
            DiscoveryFailure.of(DiscoveryFailure.Operation.RESOLVE, errorCode),
        )
    }
}
