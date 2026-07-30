package app.synco.service

import android.content.Context
import android.content.Intent

object SyncoServiceActions {

    const val START = "app.synco.service.action.START"
    const val STOP = "app.synco.service.action.STOP"
    const val PAUSE = "app.synco.service.action.PAUSE"
    const val EXTRA_PAUSED = "app.synco.service.extra.PAUSED"

    fun start(context: Context): Intent = intentFor(context, START)

    fun stop(context: Context): Intent = intentFor(context, STOP)

    fun pause(context: Context, paused: Boolean): Intent =
        intentFor(context, PAUSE).putExtra(EXTRA_PAUSED, paused)

    private fun intentFor(context: Context, action: String): Intent =
        Intent(context.applicationContext, SyncoForegroundService::class.java).setAction(action)
}
