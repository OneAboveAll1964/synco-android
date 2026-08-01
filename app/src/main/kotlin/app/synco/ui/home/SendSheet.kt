package app.synco.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.synco.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendSheet(
    onDismiss: () -> Unit,
    onSendText: (String) -> Unit,
    onSendFile: (Uri) -> Unit,
) {
    var draft by remember { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            onSendFile(uri)
            onDismiss()
        }
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = stringResource(R.string.send_title))
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = { Text(text = stringResource(R.string.send_text_hint)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    onSendText(draft)
                    onDismiss()
                },
                enabled = draft.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.send_text))
            }
            TextButton(
                onClick = { picker.launch("*/*") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.send_file))
            }
        }
    }
}
