package app.synco.ui.permissions

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import app.synco.service.ClipboardAccessBridge

@Composable
fun rememberPermissionsController(clipboardReadElsewhere: Boolean = false): PermissionsController {
    val context = LocalContext.current
    val clipboard = remember(context) { ClipboardAccessBridge(context) }
    val notifications = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { },
    )
    var revision by remember { mutableIntStateOf(0) }
    val owner = remember(context) { context.findLifecycleOwner() }
    DisposableEffect(owner) {
        val lifecycle = owner?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) revision++
        }
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }
    val status by clipboard.status.collectAsState()
    val missing = remember(revision, status, clipboardReadElsewhere) {
        PermissionChecks.missing(context, clipboard, clipboardReadElsewhere)
    }
    return remember(missing) {
        PermissionsController(missing) { requirement ->
            when (requirement) {
                PermissionRequirement.NOTIFICATIONS ->
                    notifications.launch(NotificationAccess.PERMISSION)

                PermissionRequirement.ACCESSIBILITY ->
                    context.open(ClipboardAccessBridge.settingsIntent(context))

                PermissionRequirement.BATTERY_EXEMPTION ->
                    context.openBatteryRequest()
            }
        }
    }
}

private fun Context.open(intent: Intent) {
    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

private fun Context.openBatteryRequest() {
    val asked = runCatching { open(BatteryOptimisation.requestIntent(this)) }.isSuccess
    if (!asked) runCatching { open(BatteryOptimisation.settingsIntent()) }
}
