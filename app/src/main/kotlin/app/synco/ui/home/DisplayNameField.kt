package app.synco.ui.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import app.synco.R

@Composable
fun DisplayNameField(
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember(displayName) { mutableStateOf(displayName) }
    val trimmed = draft.trim()
    val changed = trimmed.isNotEmpty() && trimmed != displayName
    OutlinedTextField(
        value = draft,
        onValueChange = { draft = it },
        modifier = modifier.fillMaxWidth(),
        label = { Text(text = stringResource(R.string.identity_name_label)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = { if (changed) onDisplayNameChange(trimmed) },
        ),
        trailingIcon = {
            if (changed) {
                IconButton(onClick = { onDisplayNameChange(trimmed) }) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = stringResource(R.string.identity_name_save),
                    )
                }
            }
        },
    )
}
