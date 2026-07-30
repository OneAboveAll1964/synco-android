package app.synco.storage

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

internal val StorageJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = false
}

internal val DirectionsMapSerializer: KSerializer<Map<String, PeerDirections>> =
    MapSerializer(String.serializer(), PeerDirections.serializer())

internal val TrustedPeerMapSerializer: KSerializer<Map<String, TrustedPeer>> =
    MapSerializer(String.serializer(), TrustedPeer.serializer())

internal fun <T> decodeStoredOrNull(serializer: KSerializer<T>, raw: String?): T? =
    raw?.let { runCatching { StorageJson.decodeFromString(serializer, it) }.getOrNull() }
