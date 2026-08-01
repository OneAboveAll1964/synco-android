package app.synco.storage

import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import app.synco.protocol.DeviceId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DataStoreSettingsStore internal constructor(
    private val dataStore: DataStore<Preferences>,
    private val fallbackDisplayName: String = Build.MODEL,
    private val clock: () -> Long = System::currentTimeMillis,
) : SettingsStore {

    private val snapshots: Flow<SettingsSnapshot> =
        dataStore.data.map { SettingsSnapshot(it, fallbackDisplayName) }

    override val displayName: Flow<String> =
        snapshots.map { it.displayName }.distinctUntilChanged()

    override val launchOnBoot: Flow<Boolean> =
        snapshots.map { it.launchOnBoot }.distinctUntilChanged()

    override val receivedFolder: Flow<String?> =
        snapshots.map { it.receivedFolder }.distinctUntilChanged()

    override val captureTuning: Flow<CaptureTuning> =
        snapshots.map { it.captureTuning }.distinctUntilChanged()

    override val captureMode: Flow<CaptureMode> =
        snapshots.map { it.captureMode }.distinctUntilChanged()

    override val shizukuPollMillis: Flow<Long> =
        snapshots.map { it.shizukuPollMillis }.distinctUntilChanged()

    override val paused: Flow<Boolean> =
        snapshots.map { it.paused }.distinctUntilChanged()

    override val maxBlobBytes: Flow<Long> =
        snapshots.map { it.maxBlobBytes }.distinctUntilChanged()

    override val defaultPolicy: Flow<SyncPolicy> =
        snapshots.map { it.defaultPolicy }.distinctUntilChanged()

    override val policies: Flow<Map<DeviceId, SyncPolicy>> =
        snapshots.map { it.policies }.distinctUntilChanged()

    override fun policy(deviceId: DeviceId): Flow<SyncPolicy> =
        snapshots.map { it.policyFor(deviceId) }.distinctUntilChanged()

    override suspend fun setDisplayName(displayName: String) = write { preferences ->
        preferences[StorageKeys.DISPLAY_NAME] = displayName.trim()
    }

    override suspend fun setLaunchOnBoot(enabled: Boolean) = write { preferences ->
        preferences[StorageKeys.LAUNCH_ON_BOOT] = enabled
    }

    override suspend fun setReceivedFolder(treeUri: String?) = write { preferences ->
        if (treeUri.isNullOrBlank()) {
            preferences.remove(StorageKeys.RECEIVED_FOLDER)
        } else {
            preferences[StorageKeys.RECEIVED_FOLDER] = treeUri
        }
    }

    override suspend fun setCaptureMode(mode: CaptureMode) = write { preferences ->
        preferences[StorageKeys.CAPTURE_MODE] = mode.wireValue
    }

    override suspend fun setShizukuPollMillis(millis: Long) = write { preferences ->
        preferences[StorageKeys.SHIZUKU_POLL_MILLIS] = millis
    }

    override suspend fun setCaptureTuning(tuning: CaptureTuning) = write { preferences ->
        preferences[StorageKeys.CAPTURE_WAIT_MILLIS] = tuning.waitMillis
        preferences[StorageKeys.FOCUS_TIMEOUT_MILLIS] = tuning.focusTimeoutMillis
        preferences[StorageKeys.CAPTURE_ATTEMPTS] = tuning.attemptsPerGesture
        preferences[StorageKeys.GESTURE_WINDOW_MILLIS] = tuning.gestureWindowMillis
    }

    override suspend fun setPaused(paused: Boolean) = write { preferences ->
        preferences[StorageKeys.PAUSED] = paused
    }

    override suspend fun setMaxBlobBytes(maxBlobBytes: Long) = write { preferences ->
        preferences[StorageKeys.MAX_BLOB_BYTES] = maxBlobBytes.coerceAtLeast(0L)
    }

    override suspend fun setDefaultDirections(directions: PeerDirections) = write { preferences ->
        preferences[StorageKeys.DEFAULT_DIRECTIONS] =
            StorageJson.encodeToString(PeerDirections.serializer(), directions)
    }

    override suspend fun setDirections(deviceId: DeviceId, directions: PeerDirections) =
        storeDirections(deviceId, directions.copy(revision = clock()))

    override suspend fun adoptDirections(deviceId: DeviceId, directions: PeerDirections) =
        storeDirections(deviceId, directions)

    override suspend fun directionsFor(deviceId: DeviceId): PeerDirections =
        policy(deviceId).first().directions

    private suspend fun storeDirections(deviceId: DeviceId, directions: PeerDirections) =
        writeDirections { preferences, current ->
            when (deviceId.value) {
                in StoredTrustedPeers.deviceIds(preferences).orEmpty() ->
                    current + (deviceId.value to directions)
                else -> current
            }
        }

    override suspend fun clearDirections(deviceId: DeviceId) =
        writeDirections { _, current -> current - deviceId.value }

    override suspend fun pruneUntrustedDirections() =
        writeDirections { preferences, _ -> StoredDirections.trustedOnly(preferences) }

    private suspend fun writeDirections(
        transform: (Preferences, Map<String, PeerDirections>) -> Map<String, PeerDirections>,
    ) = write { preferences ->
        val current = StoredDirections.all(preferences)
        val updated = transform(preferences, current)
        if (updated != current) {
            preferences[StorageKeys.PEER_DIRECTIONS] =
                StorageJson.encodeToString(DirectionsMapSerializer, updated)
        }
    }

    private suspend fun write(transform: (MutablePreferences) -> Unit) {
        dataStore.edit { preferences -> transform(preferences) }
    }
}
