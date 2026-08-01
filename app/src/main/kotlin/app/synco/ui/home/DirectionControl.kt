package app.synco.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.synco.R
import app.synco.sync.SyncDirection

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DirectionControl(
    direction: SyncDirection,
    onDirectionChange: (SyncDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.direction_heading),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ButtonGroup(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            DirectionLabels.order.forEach { choice ->
                ToggleButton(
                    checked = choice == direction,
                    onCheckedChange = { onDirectionChange(choice) },
                    modifier = Modifier.weight(1f),
                    shapes = ToggleButtonDefaults.shapes(),
                ) {
                    Icon(
                        imageVector = DirectionLabels.iconOf(choice),
                        contentDescription = stringResource(DirectionLabels.labelOf(choice)),
                    )
                }
            }
        }
        Text(
            text = stringResource(DirectionLabels.labelOf(direction)),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = stringResource(DirectionLabels.explanationOf(direction)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
