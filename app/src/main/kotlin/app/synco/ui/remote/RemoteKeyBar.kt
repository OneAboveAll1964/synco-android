package app.synco.ui.remote

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.synco.protocol.message.RemoteInputEvent
import app.synco.remote.HidKeyboard
import app.synco.remote.RemoteKeystrokes
import app.synco.remote.RemoteModifiers

private val MODIFIERS = listOf(
    "⌘" to RemoteModifiers.META,
    "⌥" to RemoteModifiers.ALT,
    "⌃" to RemoteModifiers.CONTROL,
    "⇧" to RemoteModifiers.SHIFT,
)

private val KEYS = listOf(
    "esc" to HidKeyboard.USAGE_ESCAPE,
    "tab" to HidKeyboard.USAGE_TAB,
    "←" to HidKeyboard.USAGE_LEFT,
    "↓" to HidKeyboard.USAGE_DOWN,
    "↑" to HidKeyboard.USAGE_UP,
    "→" to HidKeyboard.USAGE_RIGHT,
)

@Composable
fun RemoteKeyBar(
    mods: Int,
    onModsChange: (Int) -> Unit,
    onInput: (List<RemoteInputEvent>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MODIFIERS.forEach { (label, bit) ->
            FilterChip(
                selected = mods and bit != 0,
                onClick = { onModsChange(mods xor bit) },
                label = { Text(text = label) },
            )
        }
        KEYS.forEach { (label, usage) ->
            FilterChip(
                selected = false,
                onClick = {
                    onInput(RemoteKeystrokes.key(usage, mods))
                    onModsChange(0)
                },
                label = { Text(text = label) },
            )
        }
    }
}
