package app.synco.storage

import androidx.datastore.preferences.core.Preferences

internal object StoredDirections {

    fun all(preferences: Preferences): Map<String, PeerDirections> =
        decodeStoredOrNull(DirectionsMapSerializer, preferences[StorageKeys.PEER_DIRECTIONS])
            ?: emptyMap()

    fun trustedOnly(preferences: Preferences): Map<String, PeerDirections> {
        val trusted = StoredTrustedPeers.deviceIds(preferences) ?: return all(preferences)
        return all(preferences).filterKeys { it in trusted }
    }
}
