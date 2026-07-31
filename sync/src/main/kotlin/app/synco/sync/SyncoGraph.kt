package app.synco.sync

import android.content.Context
import app.synco.clipboard.ClipboardCapture
import app.synco.discovery.NetworkChangeMonitor
import app.synco.discovery.NsdDiscoveryService
import app.synco.storage.SettingsStore
import app.synco.storage.SyncoStorage
import app.synco.storage.TrustedPeerStore
import app.synco.transfer.DocumentTreeDestination
import app.synco.transport.SyncoClient
import app.synco.transport.SyncoSocketFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class SyncoGraph private constructor(
    val engine: SyncEngine,
    val commands: SyncCommands,
    val state: StateFlow<SyncState>,
    val settings: SettingsStore,
    val trustedPeers: TrustedPeerStore,
    val clipboard: ClipboardCapture,
) {
    companion object {
        fun create(context: Context, scope: CoroutineScope): SyncoGraph {
            val application = context.applicationContext
            val storage = SyncoStorage.create(application)
            val transfers = TransferLayer(application)
            val blobLimit = BlobSizeLimit(storage.settings, scope)
            val captureWait = CaptureWait(storage.settings, scope)
            val clipboard = ClipboardLayer(application, transfers, blobLimit::bytes, captureWait::millis)
            val state = SyncStateHolder()
            val folder = ReceivedFolder(storage.settings, scope)
            val destination = DocumentTreeDestination(
                resolver = application.contentResolver,
                treeUri = folder::uri,
                folderLabel = folder::label,
            )
            val pairings = PairingCoordinator(storage.trustedPeers, state)
            val sockets = SyncoSocketFactory()
            val discovery = NsdDiscoveryService.create(application, scope)
            val bootstrap = EngineBootstrap(
                identity = storage.identity,
                settings = storage.settings,
                trustedPeers = storage.trustedPeers,
                endpoints = SessionEndpointFactory(
                    sockets = sockets,
                    client = SyncoClient(sockets),
                    trustedPeers = TrustedPeerKeys(storage.trustedPeers),
                    approval = pairings,
                ),
                discovery = discovery,
                routers = ClipRouterFactory(
                    clipboard = clipboard.sink,
                    transfers = transfers.gateway,
                    blobs = TransferBlobSender(transfers.gateway),
                    events = state,
                    destination = destination,
                    announcer = ToastAnnouncer(application),
                ),
                pairings = pairings,
                pipelines = EnginePipelines(
                    clipboard = ClipboardPipeline(
                        capture = clipboard.capture,
                        dispatcher = OutboundClipDispatcher(transfers.gateway),
                        state = state,
                    ),
                    policies = PolicyPipeline(storage.settings, state),
                    network = NetworkPipeline(
                        network = NetworkChangeMonitor.create(application),
                        discovery = discovery,
                        state = state,
                    ),
                ),
                transfers = transfers.gateway,
                state = state,
            )
            val engine = SyncEngine(bootstrap, state, scope)
            return SyncoGraph(
                engine = engine,
                commands = SyncCommands(
                    engine = engine,
                    identity = storage.identity,
                    settings = storage.settings,
                    trustedPeers = storage.trustedPeers,
                    pairings = pairings,
                    transfers = transfers.gateway,
                    clipboard = clipboard.capture,
                    scope = scope,
                ),
                state = state.state,
                settings = storage.settings,
                trustedPeers = storage.trustedPeers,
                clipboard = clipboard.capture,
            )
        }
    }
}
