package app.synco.ui.identity

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.synco.R

@Composable
fun IdentityResetButton(
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirming by remember { mutableStateOf(false) }
    TextButton(onClick = { confirming = true }, modifier = modifier) {
        Text(text = stringResource(R.string.identity_reset_action))
    }
    if (confirming) {
        IdentityResetDialog(
            onConfirm = {
                confirming = false
                onConfirm()
            },
            onDismiss = { confirming = false },
        )
    }
}

@Composable
private fun IdentityResetDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.identity_reset_title)) },
        text = { Text(text = stringResource(R.string.identity_reset_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.identity_reset_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.identity_reset_cancel))
            }
        },
    )
}
