package app.synco.ui.history

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal data class HistoryToneColors(val container: Color, val content: Color)

@Composable
internal fun historyToneColors(tone: HistoryTone): HistoryToneColors {
    val scheme = MaterialTheme.colorScheme
    return when (tone) {
        HistoryTone.POSITIVE -> HistoryToneColors(
            container = scheme.primaryContainer,
            content = scheme.onPrimaryContainer,
        )

        HistoryTone.NEUTRAL -> HistoryToneColors(
            container = scheme.surfaceContainerHighest,
            content = scheme.onSurfaceVariant,
        )

        HistoryTone.NEGATIVE -> HistoryToneColors(
            container = scheme.errorContainer,
            content = scheme.onErrorContainer,
        )
    }
}
