package app.synco

import android.content.Context
import app.synco.service.SyncoServiceLauncher

class SyncServiceGateway(context: Context) {

    private val application = context.applicationContext

    fun start(): Boolean = SyncoServiceLauncher.start(application)

    fun stop(): Boolean = SyncoServiceLauncher.stop(application)
}
