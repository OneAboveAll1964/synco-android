package app.synco.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

object SyncoServiceLauncher {

    fun start(context: Context): Boolean = dispatch(context, SyncoServiceActions.start(context))

    fun stop(context: Context): Boolean = dispatch(context, SyncoServiceActions.stop(context))

    fun setPaused(context: Context, paused: Boolean): Boolean =
        dispatch(context, SyncoServiceActions.pause(context, paused))

    private fun dispatch(context: Context, intent: Intent): Boolean = try {
        ContextCompat.startForegroundService(context.applicationContext, intent)
        true
    } catch (denied: IllegalStateException) {
        false
    } catch (denied: SecurityException) {
        false
    }
}
