package app.synco.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.CompletableDeferred

internal class NsdRegistrationListener : NsdManager.RegistrationListener {

    private val registered = CompletableDeferred<NsdServiceInfo>()
    private val unregistered = CompletableDeferred<Unit>()

    suspend fun awaitRegistered(): NsdServiceInfo = registered.await()

    suspend fun awaitUnregistered() {
        unregistered.await()
    }

    override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
        registered.complete(serviceInfo)
    }

    override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
        registered.completeExceptionally(
            DiscoveryFailure.of(DiscoveryFailure.Operation.REGISTER, errorCode),
        )
    }

    override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
        unregistered.complete(Unit)
    }

    override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
        unregistered.completeExceptionally(
            DiscoveryFailure.of(DiscoveryFailure.Operation.UNREGISTER, errorCode),
        )
    }
}
