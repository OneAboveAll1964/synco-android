package app.synco.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import app.synco.protocol.DiscoveryConstants
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class NsdAdvertiser(private val nsdManager: NsdManager) {

    private val guard = Mutex()
    private var current: Registration? = null

    suspend fun advertise(port: Int, identity: AdvertisedIdentity) {
        guard.withLock {
            val existing = current
            if (existing != null && existing.port == port && existing.identity == identity) return
            if (existing != null) release(existing)
            current = register(port, identity)
        }
    }

    suspend fun withdraw() {
        guard.withLock {
            current?.let { release(it) }
            current = null
        }
    }

    private suspend fun register(port: Int, identity: AdvertisedIdentity): Registration {
        val listener = NsdRegistrationListener()
        nsdManager.registerService(serviceInfo(port, identity), NsdManager.PROTOCOL_DNS_SD, listener)
        val registered = withTimeoutOrNull(DiscoveryTuning.REGISTER_TIMEOUT_MILLIS) { listener.awaitRegistered() }
        if (registered == null) {
            unregisterQuietly(listener)
            throw DiscoveryFailure.timedOut(DiscoveryFailure.Operation.REGISTER)
        }
        return Registration(port, identity, listener)
    }

    private suspend fun release(registration: Registration) = withContext(NonCancellable) {
        unregisterQuietly(registration.listener)
        withTimeoutOrNull(DiscoveryTuning.UNREGISTER_TIMEOUT_MILLIS) {
            runCatching { registration.listener.awaitUnregistered() }
        }
        Unit
    }

    private fun unregisterQuietly(listener: NsdRegistrationListener) {
        runCatching { nsdManager.unregisterService(listener) }
    }

    private fun serviceInfo(port: Int, identity: AdvertisedIdentity): NsdServiceInfo =
        NsdServiceInfo().apply {
            serviceName = identity.serviceName
            serviceType = DiscoveryConstants.SERVICE_TYPE
            this.port = port
            TxtRecords.build(identity).forEach { (key, value) -> setAttribute(key, value) }
        }

    private class Registration(
        val port: Int,
        val identity: AdvertisedIdentity,
        val listener: NsdRegistrationListener,
    )
}
