package app.synco.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import app.synco.sync.SyncState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SyncoTileService : TileService() {

    private var listening: CoroutineScope? = null

    override fun onStartListening() {
        super.onStartListening()
        val graph = syncoGraphOrNull() ?: return
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        listening = scope
        scope.launch { graph.state.collect(::render) }
    }

    override fun onStopListening() {
        listening?.cancel()
        listening = null
        super.onStopListening()
    }

    override fun onClick() {
        val running = syncoGraphOrNull()?.state?.value?.running == true
        if (running) SyncoServiceLauncher.stop(this) else SyncoServiceLauncher.start(this)
    }

    private fun render(state: SyncState) {
        val tile = qsTile ?: return
        tile.state = if (state.running && !state.paused) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.synco_tile_label)
        tile.subtitle = SyncoStatusText.of(this, state)
        tile.updateTile()
    }
}
