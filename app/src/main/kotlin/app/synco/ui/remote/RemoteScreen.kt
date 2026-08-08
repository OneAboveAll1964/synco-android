package app.synco.ui.remote

import android.view.Surface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.synco.R
import app.synco.protocol.message.RemoteInputEvent
import app.synco.sync.RemoteState

@Composable
fun RemoteScreen(
    state: RemoteState,
    onSurfaceReady: (Surface) -> Unit,
    onSurfaceLost: () -> Unit,
    onInput: (List<RemoteInputEvent>) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var keyboard by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        when (state) {
            is RemoteState.Streaming -> {
                RemoteVideoSurface(
                    onSurfaceReady = onSurfaceReady,
                    onSurfaceLost = onSurfaceLost,
                    modifier = Modifier.fillMaxSize(),
                )
                RemoteTouchLayer(onInput = onInput, modifier = Modifier.fillMaxSize())
                if (keyboard) {
                    RemoteKeyboardLayer(
                        onInput = onInput,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }

            is RemoteState.Rejected -> RemoteMessage(
                text = stringResource(remoteRejectionTextRes(state.reason)),
            )

            else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.remote_close), tint = Color.White)
            }
            if (state is RemoteState.Streaming && state.input) {
                IconButton(onClick = { keyboard = !keyboard }) {
                    Icon(Icons.Filled.Keyboard, contentDescription = stringResource(R.string.remote_keyboard), tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun RemoteMessage(text: String) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = text, color = Color.White, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
