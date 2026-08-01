package app.synco.storage

import androidx.datastore.preferences.core.Preferences
import app.synco.protocol.DeviceId
import app.synco.protocol.ProtocolConstants

internal class SettingsSnapshot(preferences: Preferences, fallbackDisplayName: String) {

    val displayName: String = preferences[StorageKeys.DISPLAY_NAME]
        ?.takeIf { it.isNotBlank() }
        ?: fallbackDisplayName

    val launchOnBoot: Boolean = preferences[StorageKeys.LAUNCH_ON_BOOT] ?: false

    val receivedFolder: String? = preferences[StorageKeys.RECEIVED_FOLDER]?.takeIf { it.isNotBlank() }

    val captureMode: CaptureMode = CaptureMode.parse(preferences[StorageKeys.CAPTURE_MODE])


    val captureTuning: CaptureTuning = CaptureTuning(
        waitMillis = preferences[StorageKeys.CAPTURE_WAIT_MILLIS] ?: CaptureWaitChoice.DEFAULT.millis,
        focusTimeoutMillis = preferences[StorageKeys.FOCUS_TIMEOUT_MILLIS]
            ?: FocusTimeoutChoice.DEFAULT.millis,
        attemptsPerGesture = preferences[StorageKeys.CAPTURE_ATTEMPTS] ?: AttemptsChoice.DEFAULT.attempts,
        gestureWindowMillis = preferences[StorageKeys.GESTURE_WINDOW_MILLIS]
            ?: GestureWindowChoice.DEFAULT.millis,
    )

    val paused: Boolean = preferences[StorageKeys.PAUSED] ?: false

    val maxBlobBytes: Long = preferences[StorageKeys.MAX_BLOB_BYTES]
        ?: ProtocolConstants.DEFAULT_MAX_BLOB_BYTES

    val defaultDirections: PeerDirections =
        decodeStoredOrNull(PeerDirections.serializer(), preferences[StorageKeys.DEFAULT_DIRECTIONS])
            ?: PeerDirections.ALL_ENABLED

    private val perPeerDirections: Map<String, PeerDirections> =
        StoredDirections.trustedOnly(preferences)

    val defaultPolicy: SyncPolicy get() = policyOf(defaultDirections)

    val policies: Map<DeviceId, SyncPolicy>
        get() = perPeerDirections.entries
            .mapNotNull { (raw, directions) ->
                DeviceId.parseOrNull(raw)?.let { deviceId -> deviceId to policyOf(directions) }
            }
            .toMap()

    fun policyFor(deviceId: DeviceId): SyncPolicy =
        policyOf(perPeerDirections[deviceId.value] ?: defaultDirections)

    private fun policyOf(directions: PeerDirections): SyncPolicy =
        SyncPolicy(directions = directions, paused = paused, maxBlobBytes = maxBlobBytes)
}
