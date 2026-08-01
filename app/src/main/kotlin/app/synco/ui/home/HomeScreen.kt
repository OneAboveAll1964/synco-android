package app.synco.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.synco.ui.history.ActivityScreen

@Composable
fun HomeScreen(
    state: HomeUiState,
    actions: HomeActions,
    modifier: Modifier = Modifier,
) {
    val nowMillis = remember(state.history) { System.currentTimeMillis() }
    Column(modifier = modifier.fillMaxSize()) {
        if (state.transfers.isNotEmpty()) {
            TransferList(
                transfers = state.transfers,
                onCancel = actions::cancelTransfer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
        ActivityScreen(
            history = state.history,
            nowMillis = nowMillis,
            modifier = Modifier.weight(1f),
        )
    }
}
