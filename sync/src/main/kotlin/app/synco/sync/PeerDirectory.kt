package app.synco.sync

import app.synco.protocol.DeviceId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

internal class PeerDirectory(private val connections: PeerConnectionFactory) {

    private val entries = MutableStateFlow<Map<DeviceId, PeerConnection>>(emptyMap())

    val all: List<PeerConnection> get() = entries.value.values.toList()

    val views: Flow<List<PeerView>> = entries.flatMapLatest { current ->
        PeerViews.of(current.values.toList())
    }

    fun of(deviceId: DeviceId): PeerConnection {
        entries.value[deviceId]?.let { return it }
        val created = connections.create(deviceId)
        entries.update { current -> if (current.containsKey(deviceId)) current else current + (deviceId to created) }
        val resolved = entries.value.getValue(deviceId)
        if (resolved === created) created.start()
        return resolved
    }

    fun find(deviceId: DeviceId): PeerConnection? = entries.value[deviceId]

    fun remove(deviceId: DeviceId): PeerConnection? {
        val removed = entries.value[deviceId] ?: return null
        entries.update { it - deviceId }
        return removed
    }

    fun drain(): List<PeerConnection> {
        val current = all
        entries.value = emptyMap()
        return current
    }
}
