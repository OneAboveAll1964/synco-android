package app.synco.discovery

import android.content.Context
import android.net.nsd.NsdManager
import app.synco.protocol.DeviceId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Clock
import java.time.Instant

class NsdDiscoveryService internal constructor(
    private val advertiser: NsdAdvertiser,
    private val browser: NsdBrowser,
    private val scope: CoroutineScope,
    private val clock: Clock = Clock.systemUTC(),
) : DiscoveryService {

    private val registry = PeerRegistry()
    private val guard = Mutex()
    private var browseJob: Job? = null
    private var sweepJob: Job? = null
    private var advertisement: Advertisement? = null

    override val peers: StateFlow<List<DiscoveredPeer>> get() = registry.peers

    override suspend fun start(port: Int, advertisedIdentity: AdvertisedIdentity) {
        guard.withLock {
            advertisement = Advertisement(port, advertisedIdentity)
            advertiser.advertise(port, advertisedIdentity)
            beginBrowsing(advertisedIdentity.deviceId)
        }
    }

    override suspend fun stop() {
        guard.withLock {
            endBrowsing()
            advertiser.withdraw()
            advertisement = null
        }
    }

    override suspend fun restart() {
        guard.withLock {
            val current = advertisement ?: return
            endBrowsing()
            advertiser.withdraw()
            advertiser.advertise(current.port, current.identity)
            beginBrowsing(current.identity.deviceId)
        }
    }

    private suspend fun endBrowsing() {
        sweepJob?.cancelAndJoin()
        sweepJob = null
        browseJob?.cancelAndJoin()
        browseJob = null
        registry.clear()
    }

    private fun beginBrowsing(selfDeviceId: DeviceId) {
        browseJob = scope.launch { browseUntilCancelled(selfDeviceId) }
        sweepJob = scope.launch { sweepUntilCancelled() }
    }

    private suspend fun CoroutineScope.browseUntilCancelled(selfDeviceId: DeviceId) {
        while (isActive) {
            try {
                withTimeoutOrNull(DiscoveryTuning.BROWSE_REFRESH_MILLIS) {
                    browser.events(selfDeviceId).collect { registry.record(it, selfDeviceId) }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                registry.clear()
            }
            delay(DiscoveryTuning.BROWSE_RESTART_DELAY_MILLIS)
        }
    }

    private suspend fun CoroutineScope.sweepUntilCancelled() {
        while (isActive) {
            delay(DiscoveryTuning.PEER_SWEEP_MILLIS)
            registry.dropExpired(Instant.now(clock))
        }
    }

    private class Advertisement(val port: Int, val identity: AdvertisedIdentity)

    companion object {
        fun create(context: Context, scope: CoroutineScope): NsdDiscoveryService {
            val nsdManager = requireNotNull(context.getSystemService(NsdManager::class.java)) {
                "this device has no NsdManager"
            }
            return NsdDiscoveryService(NsdAdvertiser(nsdManager), NsdBrowser(nsdManager), scope)
        }
    }
}
