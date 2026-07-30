package app.synco.storage

import android.content.Context

class SyncoStorage private constructor(
    val settings: SettingsStore,
    val trustedPeers: TrustedPeerStore,
    val identity: IdentityStore,
) {
    companion object {
        fun create(context: Context): SyncoStorage {
            val preferences = SyncoDataStore.create(context)
            return SyncoStorage(
                settings = DataStoreSettingsStore(preferences),
                trustedPeers = DataStoreTrustedPeerStore(preferences),
                identity = EncryptedIdentityStore(context),
            )
        }
    }
}
