package app.synco.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.synco.protocol.message.CapsFlags
import app.synco.storage.ClipCategory

@Composable
fun CategorySwitches(
    heading: String,
    flags: CapsFlags,
    enabled: Boolean,
    onCategoryChange: (ClipCategory, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = heading,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CategoryLabels.order.forEach { category ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(CategoryLabels.labelOf(category)),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Switch(
                    checked = CategoryLabels.enabledIn(flags, category),
                    enabled = enabled,
                    onCheckedChange = { checked -> onCategoryChange(category, checked) },
                )
            }
        }
    }
}
