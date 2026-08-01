package app.synco.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.ui.graphics.vector.ImageVector
import app.synco.R

enum class SyncoDestination(@StringRes val title: Int, val icon: ImageVector) {
    HOME(R.string.tab_home, Icons.Filled.Home),
    SERVICE(R.string.tab_service, Icons.Filled.SyncAlt),
    SETTINGS(R.string.tab_settings, Icons.Filled.Settings),
}
