package app.synco.sync

import app.synco.protocol.DeviceId
import app.synco.storage.SettingsStore
import app.synco.storage.SyncPolicy
import kotlinx.coroutines.flow.combine

internal class PolicyPipeline(
    private val settings: SettingsStore,
    private val state: SyncStateHolder,
) {
    suspend fun run(registry: PeerSessionRegistry) {
        combine(settings.defaultPolicy, settings.policies) { default, perPeer -> Snapshot(default, perPeer) }
            .collect { snapshot ->
                state.setPaused(snapshot.default.paused)
                registry.applyPolicies(snapshot.default, snapshot.perPeer)
            }
    }

    private class Snapshot(val default: SyncPolicy, val perPeer: Map<DeviceId, SyncPolicy>)
}
