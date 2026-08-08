package app.synco.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer

class ClipHistoryStore internal constructor(private val dataStore: DataStore<Preferences>) {

    val entries: Flow<List<ClipHistoryEntry>> = dataStore.data
        .map { decode(it[StorageKeys.CLIP_HISTORY]) }
        .distinctUntilChanged()

    suspend fun add(entry: ClipHistoryEntry) {
        val trimmed = entry.copy(text = entry.text.take(ClipHistoryEntry.MAX_TEXT_CHARS))
        if (trimmed.text.isBlank()) return
        dataStore.edit { preferences ->
            val current = decode(preferences[StorageKeys.CLIP_HISTORY])
            val without = current.filterNot { it.text == trimmed.text }
            val next = (listOf(trimmed) + without).take(ClipHistoryEntry.MAX_ENTRIES)
            preferences[StorageKeys.CLIP_HISTORY] = StorageJson.encodeToString(serializer, next)
        }
    }

    suspend fun clear() {
        dataStore.edit { it.remove(StorageKeys.CLIP_HISTORY) }
    }

    private fun decode(encoded: String?): List<ClipHistoryEntry> = encoded
        ?.let { runCatching { StorageJson.decodeFromString(serializer, it) }.getOrNull() }
        .orEmpty()

    private companion object {
        val serializer = ListSerializer(ClipHistoryEntry.serializer())
    }
}
