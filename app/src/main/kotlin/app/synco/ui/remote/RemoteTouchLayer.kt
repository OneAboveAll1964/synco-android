package app.synco.ui.remote

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import app.synco.protocol.message.RemoteInputEvent
import app.synco.remote.RemotePointer
import app.synco.remote.Touch
import app.synco.remote.TouchPhase
import app.synco.remote.TrackpadTranslator

@Composable
fun RemoteTouchLayer(
    onInput: (List<RemoteInputEvent>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var size by remember { mutableStateOf(Size(1f, 1f)) }
    val pointer = remember { RemotePointer() }
    val translator = remember(size) {
        TrackpadTranslator(
            viewWidth = size.width.toDouble().coerceAtLeast(1.0),
            viewHeight = size.height.toDouble().coerceAtLeast(1.0),
            pointer = pointer,
        )
    }
    Box(
        modifier = modifier
            .onSizeChanged { size = Size(it.width.toFloat().coerceAtLeast(1f), it.height.toFloat().coerceAtLeast(1f)) }
            .pointerInput(translator) {
                var down = false
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pressedChanges = event.changes.filter { it.pressed }
                        val time = event.changes.firstOrNull()?.uptimeMillis ?: 0L
                        val phase = when {
                            pressedChanges.isEmpty() -> TouchPhase.END.also { down = false }
                            !down -> TouchPhase.START.also { down = true }
                            else -> TouchPhase.MOVE
                        }
                        val touches = pressedChanges.map {
                            Touch(it.id.value.toInt(), it.position.x.toDouble(), it.position.y.toDouble())
                        }
                        val emitted = translator.onTouch(phase, touches, time)
                        if (emitted.isNotEmpty()) onInput(emitted)
                    }
                }
            },
    )
}
