package app.synco.discovery

import app.synco.protocol.DeviceId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant

internal class PeerRegistry {

    private val visible = MutableStateFlow<List<DiscoveredPeer>>(emptyList())

    val peers: StateFlow<List<DiscoveredPeer>> = visible.asStateFlow()

    fun record(event: PeerEvent, selfDeviceId: DeviceId) {
        if (event.deviceId == selfDeviceId) return
        visible.update { current ->
            val remaining = current.filterNot { it.deviceId == event.deviceId }
            when (event) {
                is PeerEvent.Appeared -> (remaining + event.peer).sortedWith(ORDER)
                is PeerEvent.Disappeared -> remaining
            }
        }
    }

    fun dropExpired(now: Instant) {
        val oldestAccepted = now.minusMillis(DiscoveryTuning.PEER_EXPIRY_MILLIS)
        visible.update { current -> current.filterNot { it.lastSeen.isBefore(oldestAccepted) } }
    }

    fun clear() {
        visible.value = emptyList()
    }

    private companion object {
        val ORDER = compareBy<DiscoveredPeer>({ it.displayName }, { it.deviceId.value })
    }
}
