package app.synco.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import app.synco.protocol.DeviceId
import app.synco.protocol.DiscoveryConstants
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.Instant

class NsdBrowser(
    private val nsdManager: NsdManager,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val resolver = NsdResolver(nsdManager)

    fun events(selfDeviceId: DeviceId): Flow<PeerEvent> = channelFlow {
        val signals = Channel<BrowseSignal>(Channel.UNLIMITED)
        val listener = NsdDiscoveryListener(signals)
        nsdManager.discoverServices(
            DiscoveryConstants.SERVICE_TYPE,
            NsdManager.PROTOCOL_DNS_SD,
            listener,
        )
        try {
            relay(signals, selfDeviceId)
        } finally {
            signals.close()
            withContext(NonCancellable) {
                runCatching { nsdManager.stopServiceDiscovery(listener) }
            }
        }
    }

    private suspend fun ProducerScope<PeerEvent>.relay(
        signals: ReceiveChannel<BrowseSignal>,
        selfDeviceId: DeviceId,
    ) {
        val announced = mutableMapOf<String, DeviceId>()
        for (signal in signals) {
            when (signal) {
                is BrowseSignal.Found -> announce(signal.serviceInfo, selfDeviceId, announced)
                is BrowseSignal.Lost -> {
                    val gone = announced.remove(signal.serviceName)
                    if (gone != null) send(PeerEvent.Disappeared(gone))
                }
                is BrowseSignal.Failed -> throw signal.cause
            }
        }
    }

    private suspend fun ProducerScope<PeerEvent>.announce(
        serviceInfo: NsdServiceInfo,
        selfDeviceId: DeviceId,
        announced: MutableMap<String, DeviceId>,
    ) {
        if (serviceInfo.serviceName == selfDeviceId.value) return
        val peer = resolvePeer(serviceInfo) ?: return
        if (peer.deviceId == selfDeviceId) return
        announced[serviceInfo.serviceName] = peer.deviceId
        send(PeerEvent.Appeared(peer))
    }

    @Suppress("DEPRECATION")
    private suspend fun resolvePeer(request: NsdServiceInfo): DiscoveredPeer? {
        val resolved = resolver.resolve(request) ?: return null
        val identity = TxtRecords.parse(resolved.attributes) ?: return null
        val host = resolved.host?.hostAddress ?: return null
        return DiscoveredPeer.of(identity, host, resolved.port, Instant.now(clock))
    }
}
