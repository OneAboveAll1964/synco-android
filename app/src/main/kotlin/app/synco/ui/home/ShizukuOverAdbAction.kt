package app.synco.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.synco.R
import app.synco.protocol.DeviceId
import app.synco.shizuku.ShizukuState

@Composable
fun ShizukuOverAdbAction(
    state: ShizukuState,
    helper: PeerRow?,
    pending: Boolean,
    onRequest: (DeviceId) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state == ShizukuState.READY || helper == null) return
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        TextButton(
            enabled = !pending,
            onClick = { onRequest(helper.deviceId) },
        ) {
            Text(text = stringResource(R.string.shizuku_start_over_adb, helper.displayName))
        }
        Text(
            text = stringResource(R.string.shizuku_start_over_adb_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
