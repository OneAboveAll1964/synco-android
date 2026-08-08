package app.synco.sync

import app.synco.protocol.DeviceId

sealed interface RemoteState {
    data object Idle : RemoteState

    data class Requesting(val deviceId: DeviceId) : RemoteState

    data class Streaming(
        val deviceId: DeviceId,
        val width: Int,
        val height: Int,
        val input: Boolean,
    ) : RemoteState

    data class Rejected(val reason: String) : RemoteState
}
