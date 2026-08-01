package app.synco.ui.home

import app.synco.clipboard.ClipboardCaptureStatus
import app.synco.shizuku.ShizukuState
import app.synco.storage.TrustedPeer
import app.synco.sync.SyncState

internal object HomeStateMapper {

    private val ORDER =
        compareBy<PeerRow>({ !it.isConnected }, { it.displayName }, { it.deviceId.value })

    fun map(
        state: SyncState,
        preferences: HomePreferences,
        policies: PeerPolicies,
        trusted: List<TrustedPeer>,
        clipboardStatus: ClipboardCaptureStatus,
        shizukuState: ShizukuState,
    ): HomeUiState = HomeUiState(
        running = state.running,
        paused = preferences.paused,
        launchOnBoot = preferences.launchOnBoot,
        displayName = preferences.displayName,
        receivedFolder = preferences.receivedFolder,
        maxBlobBytes = preferences.maxBlobBytes,
        captureTuning = preferences.captureTuning,
        captureMode = preferences.captureMode,
        shizukuPollMillis = preferences.shizukuPollMillis,
        identity = state.identity,
        peers = peerRows(state, policies, trusted),
        transfers = state.transfers,
        pendingPairing = state.pendingPairings.firstOrNull(),
        problem = state.problem,
        history = state.history,
        clipboardStatus = clipboardStatus,
        shizukuState = shizukuState,
    )

    private fun peerRows(
        state: SyncState,
        policies: PeerPolicies,
        trusted: List<TrustedPeer>,
    ): List<PeerRow> {
        val live = state.peers.map { PeerRow.of(it) }
        val known = live.mapTo(mutableSetOf()) { it.deviceId }
        val offline = trusted
            .filterNot { it.deviceId in known }
            .map { PeerRow.of(it, policies.policyFor(it.deviceId)) }
        return (live + offline).sortedWith(ORDER)
    }
}
