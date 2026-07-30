package app.synco.sync

import android.net.Uri
import app.synco.storage.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ReceivedFolder(settings: SettingsStore, scope: CoroutineScope) {

    @Volatile
    private var current: Uri? = null

    init {
        settings.receivedFolder
            .onEach { stored -> current = stored?.let { runCatching { Uri.parse(it) }.getOrNull() } }
            .launchIn(scope)
    }

    fun uri(): Uri? = current

    fun label(): String = current?.let(FolderLabels::of).orEmpty()
}
