package app.synco.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import app.synco.R
import app.synco.storage.CaptureMode
import app.synco.sync.QRPairPayload
import app.synco.ui.home.HomeScreen
import app.synco.ui.home.HomeViewModel
import app.synco.ui.home.HomeViewModelFactory
import app.synco.ui.home.QRPairFab
import app.synco.ui.home.SendSheet
import app.synco.ui.home.ServiceScreen
import app.synco.ui.home.SettingsScreen
import app.synco.ui.home.shizukuStartMessageRes
import app.synco.ui.home.homeStatusText
import app.synco.ui.pairing.PairingDialog
import app.synco.ui.permissions.rememberPermissionsController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
    val scope = rememberCoroutineScope()
    val resources = LocalContext.current.resources
    LaunchedEffect(state.shizukuStart) {
        val report = state.shizukuStart ?: return@LaunchedEffect
        snackbars.showSnackbar(resources.getString(shizukuStartMessageRes(report)))
        model.clearShizukuStart()
    }
    val onQRScanned: (String) -> Unit = { text ->
        val payload = QRPairPayload.parse(text)
        scope.launch {
            if (payload == null) {
                snackbars.showSnackbar(resources.getString(R.string.qr_pair_bad_code))
            } else {
                model.pairWithQR(payload)
                snackbars.showSnackbar(
                    resources.getString(R.string.qr_pair_done, payload.displayName),
                )
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbars) },
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = stringResource(R.string.app_name)) })
        },
        bottomBar = {
            ShortNavigationBar {
                SyncoDestination.entries.forEach { entry ->
                    ShortNavigationBarItem(
                        selected = entry == destination,
                        onClick = { destination = entry },
                        icon = { Icon(imageVector = entry.icon, contentDescription = null) },
                        label = { Text(text = stringResource(entry.title)) },
                        iconPosition = NavigationItemIconPosition.Start,
                    )
                }
            }
        },
        floatingActionButton = {
            when (destination) {
                SyncoDestination.HOME -> FloatingActionButton(onClick = { sending = true }) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = stringResource(R.string.send_title),
                    )
                }

                SyncoDestination.SERVICE -> QRPairFab(onScanned = onQRScanned)
                SyncoDestination.SETTINGS -> Unit
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
