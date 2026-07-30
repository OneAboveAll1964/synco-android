package app.synco.discovery

import android.net.nsd.NsdServiceInfo

internal sealed interface BrowseSignal {

    data class Found(val serviceInfo: NsdServiceInfo) : BrowseSignal

    data class Lost(val serviceName: String) : BrowseSignal

    data class Failed(val cause: DiscoveryFailure) : BrowseSignal
}
