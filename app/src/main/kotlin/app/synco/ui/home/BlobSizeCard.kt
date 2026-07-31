package app.synco.ui.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.synco.R
import app.synco.storage.BlobSizeChoice

@Composable
fun BlobSizeCard(
    maxBlobBytes: Long,
    onMaxBlobBytesChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = BlobSizeChoice.nearest(maxBlobBytes)
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.blob_size_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.blob_size_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BlobSizeChoice.entries.forEach { choice ->
                    FilterChip(
                        selected = choice == selected,
                        onClick = { onMaxBlobBytesChange(choice.bytes) },
                        label = { Text(text = ByteSizeText.of(choice.bytes)) },
                    )
                }
            }
        }
    }
}
