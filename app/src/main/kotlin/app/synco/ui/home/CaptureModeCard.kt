package app.synco.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.synco.R
import app.synco.shizuku.ShizukuState
import app.synco.storage.CaptureMode

@Composable
fun CaptureModeCard(
    mode: CaptureMode,
    shizukuState: ShizukuState,
    onModeChange: (CaptureMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.capture_mode_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.capture_mode_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = mode == CaptureMode.ACCESSIBILITY,
                    onClick = { onModeChange(CaptureMode.ACCESSIBILITY) },
                    label = { Text(text = stringResource(R.string.capture_mode_accessibility)) },
                )
                FilterChip(
                    selected = mode == CaptureMode.SHIZUKU,
                    onClick = { onModeChange(CaptureMode.SHIZUKU) },
                    label = { Text(text = stringResource(R.string.capture_mode_shizuku)) },
                )
            }
            if (mode == CaptureMode.SHIZUKU) {
                Text(
                    text = stringResource(shizukuStateTextRes(shizukuState)),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (shizukuState.isUsable) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
                ShizukuAction(state = shizukuState)
            }
        }
    }
}
