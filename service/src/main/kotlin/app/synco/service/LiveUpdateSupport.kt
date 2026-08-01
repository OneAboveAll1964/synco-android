package app.synco.service

import android.app.NotificationManager
import android.os.Build

object LiveUpdateSupport {

    fun isAvailable(manager: NotificationManager?): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) return false
        val notifications = manager ?: return false
        return runCatching { notifications.canPostPromotedNotifications() }.getOrDefault(false)
    }
}
