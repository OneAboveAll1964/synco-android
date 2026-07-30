package app.synco.storage

import androidx.datastore.preferences.core.Preferences

internal object StoredTrustedPeers {

    fun map(preferences: Preferences): Map<String, TrustedPeer> = decoded(preferences) ?: emptyMap()

    fun deviceIds(preferences: Preferences): Set<String>? = decoded(preferences)?.keys

    private fun decoded(preferences: Preferences): Map<String, TrustedPeer>? {
        val raw = preferences[StorageKeys.TRUSTED_PEERS] ?: return emptyMap()
        return decodeStoredOrNull(TrustedPeerMapSerializer, raw)
    }
}
