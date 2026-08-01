package app.synco.ui.home

import app.synco.storage.CaptureMode
import app.synco.storage.CaptureTuning
import app.synco.storage.SettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class HomePreferences(
    val displayName: String,
    val launchOnBoot: Boolean,
    val paused: Boolean,
    val receivedFolder: String?,
    val maxBlobBytes: Long,
    val captureTuning: CaptureTuning,
    val captureMode: CaptureMode,
) {
    companion object {
        fun flowOf(settings: SettingsStore): Flow<HomePreferences> = combine(
            combine(settings.displayName, settings.launchOnBoot, settings.paused, ::Triple),
            settings.receivedFolder,
            settings.maxBlobBytes,
            combine(settings.captureTuning, settings.captureMode, ::Pair),
        ) { device, receivedFolder, maxBlobBytes, capture ->
            HomePreferences(
                displayName = device.first,
                launchOnBoot = device.second,
                paused = device.third,
                receivedFolder = receivedFolder,
                maxBlobBytes = maxBlobBytes,
                captureTuning = capture.first,
                captureMode = capture.second,
            )
        }
    }
}
