package app.synco.ui.permissions

import androidx.annotation.StringRes
import app.synco.R

enum class PermissionRequirement(
    @param:StringRes val titleRes: Int,
    @param:StringRes val explanationRes: Int,
    @param:StringRes val actionRes: Int,
) {
    NOTIFICATIONS(
        titleRes = R.string.permission_notifications_title,
        explanationRes = R.string.permission_notifications_explanation,
        actionRes = R.string.permission_notifications_action,
    ),
    ACCESSIBILITY(
        titleRes = R.string.permission_accessibility_title,
        explanationRes = R.string.permission_accessibility_explanation,
        actionRes = R.string.permission_accessibility_action,
    ),
    BATTERY_EXEMPTION(
        titleRes = R.string.permission_battery_title,
        explanationRes = R.string.permission_battery_explanation,
        actionRes = R.string.permission_battery_action,
    ),
}
