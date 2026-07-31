package app.synco.sync

import app.synco.protocol.ProtocolConstants
import app.synco.storage.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class BlobSizeLimit(settings: SettingsStore, scope: CoroutineScope) {

    @Volatile
    private var current = ProtocolConstants.DEFAULT_MAX_BLOB_BYTES

    init {
        settings.maxBlobBytes.onEach { current = it }.launchIn(scope)
    }

    fun bytes(): Long = current
}
