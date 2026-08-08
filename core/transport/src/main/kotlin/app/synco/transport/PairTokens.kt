package app.synco.transport

import app.synco.protocol.DeviceId
import java.util.concurrent.ConcurrentHashMap

object PairTokens {

    private val offered = ConcurrentHashMap<DeviceId, String>()

    fun offer(deviceId: DeviceId, token: String) {
        offered[deviceId] = token
    }

    fun tokenFor(deviceId: DeviceId): String? = offered[deviceId]

    fun clear(deviceId: DeviceId) {
        offered.remove(deviceId)
    }

    fun clearAll() = offered.clear()
}
