package app.synco.storage

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

internal object StorageKeys {
    val DISPLAY_NAME = stringPreferencesKey("display_name")
    val LAUNCH_ON_BOOT = booleanPreferencesKey("launch_on_boot")
    val PAUSED = booleanPreferencesKey("paused")
    val MAX_BLOB_BYTES = longPreferencesKey("max_blob_bytes")
    val DEFAULT_DIRECTIONS = stringPreferencesKey("default_directions")
    val PEER_DIRECTIONS = stringPreferencesKey("peer_directions")
    val TRUSTED_PEERS = stringPreferencesKey("trusted_peers")
    val RECEIVED_FOLDER = stringPreferencesKey("received_folder")
    val CAPTURE_WAIT_MILLIS = longPreferencesKey("capture_wait_millis")
    val FOCUS_TIMEOUT_MILLIS = longPreferencesKey("focus_timeout_millis")
    val CAPTURE_ATTEMPTS = intPreferencesKey("capture_attempts")
    val GESTURE_WINDOW_MILLIS = longPreferencesKey("gesture_window_millis")
    val CAPTURE_MODE = stringPreferencesKey("capture_mode")
}
