package app.synco.sync

import app.synco.transfer.TransferProgress
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SyncStateHolder : SyncEventSink, ShizukuStartSink, ShizukuStartReports, TransferRows {

    private val current = MutableStateFlow(SyncState.IDLE)

    private val problems = MutableStateFlow<Set<SyncProblem>>(emptySet())

    val state: StateFlow<SyncState> = current.asStateFlow()

    fun setRunning(running: Boolean) {
        current.update { it.copy(running = running) }
    }

    fun setPaused(paused: Boolean) {
        current.update { it.copy(paused = paused) }
    }

    fun setIdentity(identity: DeviceIdentity?) {
        current.update { it.copy(identity = identity) }
    }

    fun raise(problem: SyncProblem) {
        problems.update { it + problem }
        publishProblem()
    }

    fun clear(problem: SyncProblem) {
        problems.update { it - problem }
        publishProblem()
    }

    fun publishPeers(peers: List<PeerView>) {
        current.update { it.copy(peers = peers) }
    }

    fun publishPendingPairings(pending: List<PendingPairing>) {
        current.update { it.copy(pendingPairings = pending) }
    }

    override fun dropTransfer(transferId: UUID) {
        current.update { snapshot ->
            snapshot.copy(transfers = snapshot.transfers.filterNot { it.transferId == transferId })
        }
    }

    fun recordTransfer(progress: TransferProgress, atMillis: Long = System.currentTimeMillis()) {
        val view = TransferView.of(progress, atMillis)
        current.update { snapshot ->
            val others = snapshot.transfers
                .filterNot { it.transferId == view.transferId }
                .filter { isFresh(it, atMillis) }
            snapshot.copy(transfers = if (view.isFinished) others else others + view)
        }
    }

    fun reapStaleTransfers(
        liveIds: Set<UUID> = emptySet(),
        atMillis: Long = System.currentTimeMillis(),
    ) {
        current.update { snapshot ->
            val live = snapshot.transfers.filter { it.transferId in liveIds && isFresh(it, atMillis) }
            if (live.size == snapshot.transfers.size) snapshot else snapshot.copy(transfers = live)
        }
    }

    private fun isFresh(view: TransferView, atMillis: Long): Boolean =
        atMillis - view.updatedAtMillis < STALE_TRANSFER_MILLIS

    override fun record(event: SyncEvent) {
        current.update { state ->
            state.copy(
                lastEvent = event,
                history = (listOf(event) + state.history).take(HISTORY_LIMIT),
            )
        }
    }

    override fun report(report: ShizukuStartReport) {
        current.update { it.copy(shizukuStart = report) }
    }

    override fun clear() {
        current.update { it.copy(shizukuStart = null) }
    }

    fun clearPeers() {
        current.update {
            it.copy(peers = emptyList(), pendingPairings = emptyList(), transfers = emptyList())
        }
    }

    private fun publishProblem() {
        val blocking = problems.value.minByOrNull { it.ordinal }
        current.update { it.copy(problem = blocking) }
    }

    private companion object {
        const val STALE_TRANSFER_MILLIS = 20_000L
        const val HISTORY_LIMIT = 60
    }
}
