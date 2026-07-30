package app.synco.ui.home

import app.synco.protocol.DeviceId
import app.synco.storage.ClipCategory
import app.synco.sync.SyncDirection
import java.util.UUID

interface HomeActions {

    fun setRunning(running: Boolean)

    fun setPaused(paused: Boolean)

    fun setLaunchOnBoot(enabled: Boolean)

    fun setDisplayName(displayName: String)

    fun setDirection(deviceId: DeviceId, direction: SyncDirection)

    fun setSendEnabled(deviceId: DeviceId, category: ClipCategory, enabled: Boolean)

    fun setReceiveEnabled(deviceId: DeviceId, category: ClipCategory, enabled: Boolean)

    fun approvePairing(deviceId: DeviceId)

    fun rejectPairing(deviceId: DeviceId)

    fun forgetPeer(deviceId: DeviceId)

    fun reconnectPeer(deviceId: DeviceId)

    fun cancelTransfer(transferId: UUID)

    fun resetIdentity()
}
