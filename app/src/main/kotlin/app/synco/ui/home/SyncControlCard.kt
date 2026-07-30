package app.synco.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.synco.R

@Composable
fun SyncControlCard(
    running: Boolean,
    paused: Boolean,
    launchOnBoot: Boolean,
    statusText: String,
    onRunningChange: (Boolean) -> Unit,
    onPausedChange: (Boolean) -> Unit,
    onLaunchOnBootChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SwitchRow(
                title = stringResource(R.string.control_running_title),
                subtitle = statusText,
                checked = running,
                enabled = true,
                onCheckedChange = onRunningChange,
            )
            SwitchRow(
                title = stringResource(R.string.control_paused_title),
                subtitle = stringResource(R.string.control_paused_subtitle),
                checked = paused,
                enabled = running,
                onCheckedChange = onPausedChange,
            )
            SwitchRow(
                title = stringResource(R.string.control_boot_title),
                subtitle = stringResource(R.string.control_boot_subtitle),
                checked = launchOnBoot,
                enabled = true,
                onCheckedChange = onLaunchOnBootChange,
            )
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = LocalContentColor.current.copy(alpha = SUBTITLE_ALPHA),
            )
        }
        Switch(checked = checked, enabled = enabled, onCheckedChange = onCheckedChange)
    }
}

private const val SUBTITLE_ALPHA = 0.75f
