package app.synco.sync

import app.synco.clipboard.ClipboardSnapshot
import app.synco.protocol.DeviceId
import app.synco.storage.IdentityUnavailable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SyncEngine internal constructor(
    private val bootstrap: EngineBootstrap,
    private val state: SyncStateHolder,
    private val scope: CoroutineScope,
) {
    private val guard = Mutex()

    @Volatile
    private var runtime: EngineRuntime? = null

    val isRunning: Boolean get() = runtime != null

    suspend fun start() {
        guard.withLock {
            if (runtime != null) return@withLock
            runtime = launchRuntime()
            state.setRunning(runtime != null)
        }
    }

    suspend fun stop() {
        guard.withLock {
            val current = runtime ?: return@withLock
            runtime = null
            current.shutdown()
            state.clearPeers()
            state.setRunning(false)
        }
    }

    suspend fun restart() {
        stop()
        start()
    }

    suspend fun restartDiscovery() {
        runtime?.restartDiscovery()
    }

    suspend fun broadcast(snapshot: ClipboardSnapshot) {
        val runtime = runtime ?: return
        runtime.dispatch(snapshot)
    }

    suspend fun requestShizukuStart(deviceId: DeviceId) {
        runtime?.registry?.routerOf(deviceId)?.requestShizukuStart()
    }

    suspend fun startRemote(deviceId: DeviceId, request: app.synco.protocol.message.RemoteStart) {
        runtime?.registry?.routerOf(deviceId)?.startRemote(request)
    }

    suspend fun sendRemoteInput(deviceId: DeviceId, events: List<app.synco.protocol.message.RemoteInputEvent>) {
        runtime?.registry?.routerOf(deviceId)?.sendRemoteInput(events)
    }

    suspend fun stopRemote(deviceId: DeviceId) {
        runtime?.registry?.routerOf(deviceId)?.stopRemote()
    }

    fun reconnect(deviceId: DeviceId) {
        runtime?.registry?.reconnect(deviceId)
    }

    suspend fun forget(deviceId: DeviceId) {
        runtime?.registry?.forget(deviceId)
    }

    private suspend fun launchRuntime(): EngineRuntime? = try {
        bootstrap.launch(scope)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (unavailable: IdentityUnavailable) {
        report(SyncProblem.IDENTITY_UNREADABLE, unavailable)
    } catch (failure: Exception) {
        report(SyncProblem.LISTENER_UNAVAILABLE, failure)
    }

    private fun report(problem: SyncProblem, failure: Exception): EngineRuntime? {
        state.raise(problem)
        state.record(SyncEvent.of(SyncEvent.Kind.ENGINE_FAILED, detail = failure.message))
        return null
    }
}
