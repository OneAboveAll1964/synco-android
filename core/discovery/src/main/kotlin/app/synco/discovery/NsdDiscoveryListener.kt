package app.synco.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.channels.SendChannel

internal class NsdDiscoveryListener(
    private val signals: SendChannel<BrowseSignal>,
) : NsdManager.DiscoveryListener {

    override fun onDiscoveryStarted(serviceType: String) = Unit

    override fun onDiscoveryStopped(serviceType: String) = Unit

    override fun onServiceFound(serviceInfo: NsdServiceInfo) {
        signals.trySend(BrowseSignal.Found(serviceInfo))
    }

    override fun onServiceLost(serviceInfo: NsdServiceInfo) {
        signals.trySend(BrowseSignal.Lost(serviceInfo.serviceName))
    }

    override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
        signals.trySend(
            BrowseSignal.Failed(DiscoveryFailure.of(DiscoveryFailure.Operation.DISCOVER, errorCode)),
        )
    }

    override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit
}
