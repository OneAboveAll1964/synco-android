package app.synco.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import app.synco.R
import app.synco.storage.CaptureMode
import app.synco.ui.home.HomeScreen
import app.synco.ui.home.HomeViewModel
import app.synco.ui.home.HomeViewModelFactory
import app.synco.ui.home.SendSheet
import app.synco.ui.home.ServiceScreen
import app.synco.ui.home.SettingsScreen
import app.synco.ui.home.shizukuStartMessageRes
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
    val permissions = rememberPermissionsController(
        clipboardReadElsewhere = state.captureMode == CaptureMode.SHIZUKU && state.shizukuState.isUsable,
    )
    var destination by remember { mutableStateOf(SyncoDestination.HOME) }
    var sending by remember { mutableStateOf(false) }

    val snackbars = remember { SnackbarHostState() }
    val resources = LocalContext.current.resources
    LaunchedEffect(state.shizukuStart) {
        val report = state.shizukuStart ?: return@LaunchedEffect
        snackbars.showSnackbar(resources.getString(shizukuStartMessageRes(report)))
        model.clearShizukuStart()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbars) },
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = stringResource(R.string.app_name)) })
        },
        bottomBar = {
            NavigationBar {
                SyncoDestination.entries.forEach { entry ->
                    NavigationBarItem(
                        selected = entry == destination,
                        onClick = { destination = entry },
                        icon = { Icon(imageVector = entry.icon, contentDescription = null) },
                        label = { Text(text = stringResource(entry.title)) },
                    )
                }
            }
        },
        floatingActionButton = {
            if (destination == SyncoDestination.HOME) {
                FloatingActionButton(onClick = { sending = true }) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = stringResource(R.string.send_title),
                    )
                }
            }
        },
    ) { insets ->
        when (destination) {
            SyncoDestination.HOME -> HomeScreen(
                state = state,
                actions = model,
                modifier = Modifier.padding(insets),
            )

            SyncoDestination.SERVICE -> ServiceScreen(
                state = state,
                statusText = homeStatusText(state),
                permissions = permissions,
                actions = model,
                modifier = Modifier.padding(insets),
            )

            SyncoDestination.SETTINGS -> SettingsScreen(
                state = state,
                actions = model,
                modifier = Modifier.padding(insets),
            )
        }
    }

    if (sending) {
        SendSheet(
            onDismiss = { sending = false },
            onSendText = model::sendText,
            onSendFile = model::sendFile,
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
