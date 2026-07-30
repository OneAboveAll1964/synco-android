package app.synco.ui.permissions

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings

internal object BatteryOptimisation {

    fun isExempt(context: Context): Boolean = context
        .getSystemService(PowerManager::class.java)
        ?.isIgnoringBatteryOptimizations(context.packageName) == true

    fun settingsIntent(): Intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
}
