package app.synco.service

import android.content.Context
import app.synco.sync.SyncState

internal object SyncoStatusText {

    fun of(context: Context, state: SyncState): String = when {
        !state.running -> context.getString(R.string.synco_status_stopped)
        state.paused -> context.getString(R.string.synco_status_paused)
        state.connectedPeers.isEmpty() -> context.getString(R.string.synco_status_searching)
        else -> context.getString(R.string.synco_status_connected, peerNames(state))
    }

    private fun peerNames(state: SyncState): String =
        state.connectedPeers.joinToString(separator = ", ") { it.displayName }
}
