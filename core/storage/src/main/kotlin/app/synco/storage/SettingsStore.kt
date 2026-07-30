package app.synco.storage

import app.synco.protocol.DeviceId
import kotlinx.coroutines.flow.Flow

interface SettingsStore {

    val displayName: Flow<String>

    val launchOnBoot: Flow<Boolean>

    val receivedFolder: Flow<String?>

    val paused: Flow<Boolean>

    val maxBlobBytes: Flow<Long>

    val defaultPolicy: Flow<SyncPolicy>

    val policies: Flow<Map<DeviceId, SyncPolicy>>

    fun policy(deviceId: DeviceId): Flow<SyncPolicy>

    suspend fun setDisplayName(displayName: String)

    suspend fun setLaunchOnBoot(enabled: Boolean)

    suspend fun setReceivedFolder(treeUri: String?)

    suspend fun setPaused(paused: Boolean)

    suspend fun setMaxBlobBytes(maxBlobBytes: Long)

    suspend fun setDefaultDirections(directions: PeerDirections)

    suspend fun setDirections(deviceId: DeviceId, directions: PeerDirections)

    suspend fun clearDirections(deviceId: DeviceId)

    suspend fun pruneUntrustedDirections()
}
