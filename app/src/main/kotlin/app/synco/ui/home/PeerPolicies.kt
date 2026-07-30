package app.synco.ui.home

import app.synco.protocol.DeviceId
import app.synco.storage.SettingsStore
import app.synco.storage.SyncPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class PeerPolicies(
    val fallback: SyncPolicy,
    val perPeer: Map<DeviceId, SyncPolicy>,
) {
    fun policyFor(deviceId: DeviceId): SyncPolicy = perPeer[deviceId] ?: fallback

    companion object {
        fun flowOf(settings: SettingsStore): Flow<PeerPolicies> = combine(
            settings.defaultPolicy,
            settings.policies,
        ) { fallback, perPeer -> PeerPolicies(fallback, perPeer) }
    }
}
