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
import app.synco.storage.AttemptsChoice
import app.synco.storage.CaptureMode
import app.synco.storage.CaptureTuning
import app.synco.storage.CaptureWaitChoice
import app.synco.storage.FocusTimeoutChoice
import app.synco.storage.GestureWindowChoice

@Composable
fun CaptureTuningCard(
    tuning: CaptureTuning,
    onTuningChange: (CaptureTuning) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.tuning_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.tuning_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ChoiceRow(
                label = stringResource(R.string.tuning_wait),
                options = CaptureWaitChoice.entries.map { MillisText.of(it.millis) to it.millis },
                selected = CaptureWaitChoice.nearest(tuning.waitMillis).millis,
                onSelect = { onTuningChange(tuning.copy(waitMillis = it)) },
            )
            ChoiceRow(
                label = stringResource(R.string.tuning_focus),
                options = FocusTimeoutChoice.entries.map { MillisText.of(it.millis) to it.millis },
                selected = FocusTimeoutChoice.nearest(tuning.focusTimeoutMillis).millis,
                onSelect = { onTuningChange(tuning.copy(focusTimeoutMillis = it)) },
            )
            ChoiceRow(
                label = stringResource(R.string.tuning_window),
                options = GestureWindowChoice.entries.map { MillisText.of(it.millis) to it.millis },
                selected = GestureWindowChoice.nearest(tuning.gestureWindowMillis).millis,
                onSelect = { onTuningChange(tuning.copy(gestureWindowMillis = it)) },
            )
            ChoiceRow(
                label = stringResource(R.string.tuning_attempts),
                options = AttemptsChoice.entries.map { "${it.attempts}" to it.attempts.toLong() },
                selected = AttemptsChoice.nearest(tuning.attemptsPerGesture).attempts.toLong(),
                onSelect = { onTuningChange(tuning.copy(attemptsPerGesture = it.toInt())) },
            )
        }
    }
}

@Composable
private fun ChoiceRow(
    label: String,
    options: List<Pair<String, Long>>,
    selected: Long,
    onSelect: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { (title, value) ->
                FilterChip(
                    selected = value == selected,
                    onClick = { onSelect(value) },
                    label = { Text(text = title) },
                )
            }
        }
    }
}
