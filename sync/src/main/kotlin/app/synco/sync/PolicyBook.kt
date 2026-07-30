package app.synco.sync

import app.synco.protocol.DeviceId
import app.synco.storage.SyncPolicy

internal class PolicyBook {

    @Volatile
    private var fallback: SyncPolicy = SyncPolicy.DEFAULT

    @Volatile
    private var perPeer: Map<DeviceId, SyncPolicy> = emptyMap()

    fun update(default: SyncPolicy, policies: Map<DeviceId, SyncPolicy>) {
        fallback = default
        perPeer = policies
    }

    fun policyFor(deviceId: DeviceId): SyncPolicy = perPeer[deviceId] ?: fallback
}
