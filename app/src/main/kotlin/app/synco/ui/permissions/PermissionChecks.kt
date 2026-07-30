package app.synco.ui.permissions

import android.content.Context
import app.synco.service.ClipboardAccessBridge

internal object PermissionChecks {

    fun missing(context: Context, clipboard: ClipboardAccessBridge): List<PermissionRequirement> =
        buildList {
            if (!NotificationAccess.isGranted(context)) add(PermissionRequirement.NOTIFICATIONS)
            if (!clipboard.isServiceEnabled) add(PermissionRequirement.ACCESSIBILITY)
            if (!BatteryOptimisation.isExempt(context)) add(PermissionRequirement.BATTERY_EXEMPTION)
        }
}
