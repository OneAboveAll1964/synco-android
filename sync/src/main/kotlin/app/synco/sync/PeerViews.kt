package app.synco.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

internal object PeerViews {

    private val ORDER = compareBy<PeerView>({ !it.isConnected }, { it.displayName }, { it.deviceId.value })

    fun of(connections: List<PeerConnection>): Flow<List<PeerView>> = when {
        connections.isEmpty() -> flowOf(emptyList())
        else -> combine(connections.map { it.view }) { views -> views.sortedWith(ORDER) }
    }
}
