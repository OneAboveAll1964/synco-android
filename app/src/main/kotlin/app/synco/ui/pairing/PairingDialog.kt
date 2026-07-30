package app.synco.ui.pairing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.synco.R
import app.synco.protocol.DeviceId
import app.synco.sync.PendingPairing

@Composable
fun PairingDialog(
    pairing: PendingPairing,
    onApprove: (DeviceId) -> Unit,
    onReject: (DeviceId) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onReject(pairing.deviceId) },
        icon = { Icon(imageVector = Icons.Filled.Devices, contentDescription = null) },
        title = { Text(text = stringResource(R.string.pairing_title, pairing.displayName)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = stringResource(
                        R.string.pairing_body,
                        pairing.displayName,
                        pairing.platform.wireValue,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                FingerprintBlocks(fingerprint = pairing.fingerprint)
                Text(
                    text = stringResource(R.string.pairing_compare_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onApprove(pairing.deviceId) }) {
                Text(text = stringResource(R.string.pairing_approve))
            }
        },
        dismissButton = {
            TextButton(onClick = { onReject(pairing.deviceId) }) {
                Text(text = stringResource(R.string.pairing_reject))
            }
        },
    )
}
