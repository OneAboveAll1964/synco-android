package app.synco.discovery

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    val changes: Flow<NetworkChange>
}
