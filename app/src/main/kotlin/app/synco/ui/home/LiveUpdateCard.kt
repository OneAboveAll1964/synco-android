package app.synco.ui.home

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.synco.R

@Composable
fun LiveUpdateCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    if (!needsLiveUpdatePermission(context)) return
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.live_update_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.live_update_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isSamsung()) {
                Text(
                    text = stringResource(R.string.live_update_samsung),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = { openDeveloperOptions(context) }) {
                    Text(text = stringResource(R.string.live_update_samsung_action))
                }
            }
            TextButton(onClick = { openNotificationSettings(context) }) {
                Text(text = stringResource(R.string.live_update_action))
            }
        }
    }
}

private fun needsLiveUpdatePermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) return false
    val manager = context.getSystemService(NotificationManager::class.java) ?: return false
    return runCatching { !manager.canPostPromotedNotifications() }.getOrDefault(false)
}

private fun openNotificationSettings(context: Context) {
    val direct = Intent(PROMOTED_SETTINGS_ACTION)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val opened = runCatching { context.startActivity(direct) }.isSuccess
    if (opened) return
    val fallback = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(fallback) }
}

private fun isSamsung(): Boolean = Build.MANUFACTURER.equals("samsung", ignoreCase = true)

private fun openDeveloperOptions(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

private const val PROMOTED_SETTINGS_ACTION =
    "android.settings.MANAGE_APP_PROMOTED_NOTIFICATIONS"
