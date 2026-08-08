package app.synco.ui.remote

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import app.synco.protocol.message.RemoteInputEvent
import app.synco.remote.RemoteKeystrokes

private const val PADDING_LENGTH = 16
private const val PADDING_CHAR = '\u200B'
private const val MAX_LENGTH = PADDING_LENGTH + 128

@Composable
fun RemoteKeyboardLayer(
    onInput: (List<RemoteInputEvent>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val padding = remember { PADDING_CHAR.toString().repeat(PADDING_LENGTH) }
    var typed by remember { mutableStateOf(padding) }
    var mods by remember { mutableIntStateOf(0) }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focus.requestFocus()
        keyboard?.show()
    }
    DisposableEffect(Unit) {
        onDispose { keyboard?.hide() }
    }

    Column(modifier = modifier.fillMaxWidth().imePadding().navigationBarsPadding()) {
        RemoteKeyBar(mods = mods, onModsChange = { mods = it }, onInput = onInput)
        BasicTextField(
            value = typed,
            onValueChange = { next ->
                val events = RemoteKeystrokes.edits(typed, next, mods)
                if (events.isNotEmpty()) {
                    onInput(events)
                    mods = 0
                }
                typed = if (next.length < PADDING_LENGTH || next.length > MAX_LENGTH) padding else next
            },
            textStyle = TextStyle(color = Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent),
            modifier = Modifier.size(1.dp).focusRequester(focus),
        )
    }
}
