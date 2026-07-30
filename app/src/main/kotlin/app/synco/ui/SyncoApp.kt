package app.synco.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import app.synco.R
import app.synco.ui.home.HomeScreen
import app.synco.ui.home.HomeViewModel
import app.synco.ui.home.HomeViewModelFactory
import app.synco.ui.home.homeStatusText
import app.synco.ui.pairing.PairingDialog
import app.synco.ui.permissions.rememberPermissionsController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncoApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val factory = remember(context) { HomeViewModelFactory(context) }
    val model: HomeViewModel = viewModel(factory = factory)
    val state by model.state.collectAsState()
    val permissions = rememberPermissionsController()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = stringResource(R.string.app_name)) })
        },
    ) { insets ->
        HomeScreen(
            state = state,
            statusText = homeStatusText(state),
            permissions = permissions,
            actions = model,
            modifier = Modifier.padding(insets),
        )
    }
    state.pendingPairing?.let { pairing ->
        PairingDialog(
            pairing = pairing,
            onApprove = model::approvePairing,
            onReject = model::rejectPairing,
        )
    }
}
