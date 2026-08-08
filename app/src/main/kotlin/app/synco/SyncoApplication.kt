package app.synco

import android.app.Application
import app.synco.service.SyncoGraphOwner
import app.synco.service.SyncoServiceLauncher
import app.synco.sync.SyncoGraph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SyncoApplication : Application(), SyncoGraphOwner {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var graph: SyncoGraph? = null

    override val syncoGraph: SyncoGraph
        get() = requireNotNull(graph) { "the graph is built in Application.onCreate" }

    override fun onCreate() {
        super.onCreate()
        graph = SyncoGraph.create(this, scope)
        scope.launch { startWhenConfigured() }
    }

    private suspend fun startWhenConfigured() {
        val settings = syncoGraph.settings
        if (settings.launchOnBoot.first() || settings.syncRequested.first()) {
            SyncoServiceLauncher.start(this)
        }
    }
}
