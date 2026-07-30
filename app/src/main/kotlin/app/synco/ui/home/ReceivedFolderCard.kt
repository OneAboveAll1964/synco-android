package app.synco.ui.home

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import app.synco.sync.FolderLabels

@Composable
fun ReceivedFolderCard(
    receivedFolder: String?,
    onFolderChosen: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { treeUri ->
        if (treeUri == null) return@rememberLauncherForActivityResult
        val granted = runCatching {
            context.contentResolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
        }.isSuccess
        if (granted) onFolderChosen(treeUri.toString())
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.received_folder_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = ReceivedFolderText.describe(receivedFolder),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { picker.launch(DownloadsTree.initialUri()) }) {
                    Text(text = stringResource(R.string.received_folder_choose))
                }
                if (receivedFolder != null) {
                    TextButton(onClick = { onFolderChosen(null) }) {
                        Text(text = stringResource(R.string.received_folder_clear))
                    }
                }
            }
        }
    }
}

object ReceivedFolderText {

    @Composable
    fun describe(receivedFolder: String?): String = if (receivedFolder == null) {
        stringResource(R.string.received_folder_unset)
    } else {
        stringResource(R.string.received_folder_set, FolderLabels.of(android.net.Uri.parse(receivedFolder)))
    }
}
