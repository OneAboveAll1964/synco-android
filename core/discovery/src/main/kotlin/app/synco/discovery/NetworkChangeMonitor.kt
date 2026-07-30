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
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(NetworkChange.AVAILABLE)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                trySend(NetworkChange.RECONFIGURED)
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                trySend(NetworkChange.RECONFIGURED)
            }

            override fun onLost(network: Network) {
                trySend(NetworkChange.UNAVAILABLE)
            }
        }
        connectivityManager.registerNetworkCallback(LOCAL_NETWORKS, callback)
        awaitClose {
            runCatching { connectivityManager.unregisterNetworkCallback(callback) }
        }
    }.buffer(Channel.CONFLATED)

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
