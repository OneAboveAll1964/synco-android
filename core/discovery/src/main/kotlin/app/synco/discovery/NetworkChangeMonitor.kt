package app.synco.discovery

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

class NetworkChangeMonitor(
    private val connectivityManager: ConnectivityManager,
) : NetworkMonitor {

    override val changes: Flow<NetworkChange> = callbackFlow {
        val addresses = HashMap<Network, String>()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(NetworkChange.AVAILABLE)
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                val fingerprint = fingerprintOf(linkProperties)
                if (addresses.put(network, fingerprint) == fingerprint) return
                trySend(NetworkChange.RECONFIGURED)
            }

            override fun onLost(network: Network) {
                addresses.remove(network)
                trySend(NetworkChange.UNAVAILABLE)
            }
        }
        connectivityManager.registerNetworkCallback(LOCAL_NETWORKS, callback)
        awaitClose {
            runCatching { connectivityManager.unregisterNetworkCallback(callback) }
        }
    }.buffer(Channel.CONFLATED)

    private fun fingerprintOf(linkProperties: LinkProperties): String = buildString {
        append(linkProperties.interfaceName ?: "?")
        linkProperties.linkAddresses
            .map { it.toString() }
            .sorted()
            .forEach { append('|').append(it) }
    }

    companion object {
        private val LOCAL_NETWORKS: NetworkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .build()

        fun create(context: Context): NetworkChangeMonitor {
            val connectivityManager =
                requireNotNull(context.getSystemService(ConnectivityManager::class.java)) {
                    "this device has no ConnectivityManager"
                }
            return NetworkChangeMonitor(connectivityManager)
        }
    }
}
