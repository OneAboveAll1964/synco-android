package app.synco.sync

import app.synco.clipboard.CaptureRoute
import app.synco.clipboard.ClipboardCapture
import app.synco.protocol.DeviceId
import app.synco.storage.CaptureMode
import app.synco.storage.CaptureTuning
import app.synco.storage.ClipCategory
import app.synco.storage.IdentityStore
import app.synco.storage.PeerDirections
import app.synco.storage.SettingsStore
import app.synco.storage.TrustedPeerStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class SyncCommands internal constructor(
    private val engine: SyncEngine,
    private val identity: IdentityStore,
    private val settings: SettingsStore,
    private val trustedPeers: TrustedPeerStore,
    private val pairings: PairingCoordinator,
    private val transfers: TransferGateway,
    private val clipboard: ClipboardCapture,
    private val scope: CoroutineScope,
    private val manual: app.synco.clipboard.ManualClips,
    private val events: SyncEventSink,
) {
    fun start() = fire { engine.start() }

    fun stop() = fire { engine.stop() }

    fun setPaused(paused: Boolean) = fire { settings.setPaused(paused) }

    fun setLaunchOnBoot(enabled: Boolean) = fire { settings.setLaunchOnBoot(enabled) }

    fun setReceivedFolder(treeUri: String?) = fire { settings.setReceivedFolder(treeUri) }

    fun setMaxBlobBytes(bytes: Long) = fire { settings.setMaxBlobBytes(bytes) }

    fun setCaptureTuning(tuning: CaptureTuning) = fire { settings.setCaptureTuning(tuning) }

    fun setCaptureMode(mode: CaptureMode) = fire { settings.setCaptureMode(mode) }

    fun setShizukuPollMillis(millis: Long) = fire { settings.setShizukuPollMillis(millis) }

    fun setDisplayName(displayName: String) = fire {
        settings.setDisplayName(displayName)
        if (engine.isRunning) engine.restart()
    }

    fun setDirection(deviceId: DeviceId?, direction: SyncDirection) = fire {
        edit(deviceId) { direction.applyTo(it) }
    }

    fun setSendEnabled(deviceId: DeviceId?, category: ClipCategory, enabled: Boolean) = fire {
        edit(deviceId) { PolicyEdits.withSend(it, category, enabled) }
    }

    fun setReceiveEnabled(deviceId: DeviceId?, category: ClipCategory, enabled: Boolean) = fire {
        edit(deviceId) { PolicyEdits.withReceive(it, category, enabled) }
    }

    fun approvePairing(deviceId: DeviceId) {
        pairings.decide(deviceId, approved = true)
    }

    fun rejectPairing(deviceId: DeviceId) {
        pairings.decide(deviceId, approved = false)
    }

    fun forgetPeer(deviceId: DeviceId) = fire {
        trustedPeers.remove(deviceId)
        settings.clearDirections(deviceId)
        engine.forget(deviceId)
    }

    fun reconnectPeer(deviceId: DeviceId) {
        engine.reconnect(deviceId)
    }

    fun cancelTransfer(transferId: UUID) {
        transfers.cancel(transferId)
    }

    fun sendText(value: String) = fire {
        val snapshot = manual.text(value) ?: return@fire
        engine.broadcast(snapshot)
        events.record(SyncEvent.of(SyncEvent.Kind.CLIP_SENT, detail = MANUAL_TEXT))
    }

    fun sendFile(uri: android.net.Uri) = fire {
        val snapshot = manual.file(uri, settings.maxBlobBytes.first()) ?: return@fire
        engine.broadcast(snapshot)
        events.record(SyncEvent.of(SyncEvent.Kind.CLIP_SENT, detail = MANUAL_FILE))
    }

    fun refreshClipboard() = fire {
        clipboard.captureVia(CaptureRoute.FOREGROUND_LISTENER)
    }

    fun resetIdentity() = fire {
        identity.replaceWithFreshIdentity()
        engine.restart()
    }

    private fun fire(block: suspend () -> Unit) {
        scope.launch { quietly(block) }
    }

    private suspend fun edit(deviceId: DeviceId?, change: (PeerDirections) -> PeerDirections) {
        if (deviceId == null) {
            settings.setDefaultDirections(change(settings.defaultPolicy.first().directions))
        } else {
            settings.setDirections(deviceId, change(settings.policy(deviceId).first().directions))
        }
    }

    private companion object {
        const val MANUAL_TEXT = "manual text"
        const val MANUAL_FILE = "manual file"
    }
}
