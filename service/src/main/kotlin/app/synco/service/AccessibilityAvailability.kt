package app.synco.service

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.view.accessibility.AccessibilityManager

internal object AccessibilityAvailability {

    fun component(context: Context): ComponentName =
        ComponentName(context.applicationContext, SyncoAccessibilityService::class.java)

    fun isEnabled(context: Context): Boolean {
        val manager = context.getSystemService(AccessibilityManager::class.java) ?: return false
        val expected = component(context)
        return runCatching {
            manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                .any { ComponentName.unflattenFromString(it.id) == expected }
        }.getOrDefault(false)
    }
}
