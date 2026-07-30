package app.synco.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.synco.R

@Composable
fun homeStatusText(state: HomeUiState): String {
    val connected = state.connectedPeers
    return when {
        !state.running -> stringResource(R.string.home_status_stopped)
        state.paused -> stringResource(R.string.home_status_paused)
        connected.isEmpty() -> stringResource(R.string.home_status_searching)
        else -> stringResource(
            R.string.home_status_connected,
            connected.map { it.displayName }.joinToString(", "),
        )
    }
}
