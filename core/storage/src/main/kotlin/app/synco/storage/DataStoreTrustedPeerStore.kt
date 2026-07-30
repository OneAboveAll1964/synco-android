package app.synco.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import app.synco.protocol.DeviceId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreTrustedPeerStore internal constructor(
    private val dataStore: DataStore<Preferences>,
) : TrustedPeerStore {

    override val peers: Flow<List<TrustedPeer>> = dataStore.data
        .map { preferences -> stored(preferences).values.sortedWith(ORDER) }
        .distinctUntilChanged()

    override suspend fun add(peer: TrustedPeer) {
        mutate { it + (peer.deviceId.value to peer) }
    }

    override suspend fun remove(deviceId: DeviceId) {
        mutate { it - deviceId.value }
    }

    override suspend fun find(deviceId: DeviceId): TrustedPeer? =
        stored(dataStore.data.first())[deviceId.value]

    override suspend fun updateDisplayName(deviceId: DeviceId, displayName: String) {
        mutateExisting(deviceId) { it.copy(displayName = displayName) }
    }

    override suspend fun setRejected(deviceId: DeviceId, rejected: Boolean) {
        mutateExisting(deviceId) { it.copy(rejected = rejected) }
    }

    private suspend fun mutateExisting(deviceId: DeviceId, transform: (TrustedPeer) -> TrustedPeer) {
        mutate { current ->
            val existing = current[deviceId.value] ?: return@mutate current
            current + (deviceId.value to transform(existing))
        }
    }

    private suspend fun mutate(
        transform: (Map<String, TrustedPeer>) -> Map<String, TrustedPeer>,
    ) {
        dataStore.edit { preferences ->
            val updated = transform(stored(preferences))
            preferences[StorageKeys.TRUSTED_PEERS] =
                StorageJson.encodeToString(TrustedPeerMapSerializer, updated)
        }
    }

    private fun stored(preferences: Preferences): Map<String, TrustedPeer> =
        StoredTrustedPeers.map(preferences)

    private companion object {
        val ORDER = compareBy<TrustedPeer>({ it.displayName }, { it.deviceId.value })
    }
}
