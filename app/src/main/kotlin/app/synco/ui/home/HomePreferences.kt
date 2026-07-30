package app.synco.ui.home

import app.synco.storage.SettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class HomePreferences(
    val displayName: String,
    val launchOnBoot: Boolean,
    val paused: Boolean,
) {
    companion object {
        fun flowOf(settings: SettingsStore): Flow<HomePreferences> = combine(
            settings.displayName,
            settings.launchOnBoot,
            settings.paused,
        ) { displayName, launchOnBoot, paused ->
            HomePreferences(displayName, launchOnBoot, paused)
        }
    }
}
