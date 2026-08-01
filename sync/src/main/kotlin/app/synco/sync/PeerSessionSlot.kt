package app.synco.sync

import app.synco.protocol.message.PolicySync
import app.synco.storage.PeerDirections
import app.synco.storage.SyncPolicy

import app.synco.crypto.HandshakeRole
import app.synco.protocol.DeviceId
import app.synco.protocol.message.CloseReason
import app.synco.transport.PeerSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class PeerSessionSlot(
    private val selfDeviceId: DeviceId,
    private val peerDeviceId: DeviceId,
    private val role: HandshakeRole,
    private val settings: PeerSettings,
    private val routers: ClipRouterFactory,
) {
    private val guard = Mutex()
    private val liveState = MutableStateFlow(false)

    @Volatile
    private var current: LiveSession? = null

    val live: StateFlow<Boolean> = liveState.asStateFlow()

    val router: ClipRouter? get() = current?.router

    suspend fun claim(session: PeerSession, origin: SessionOrigin): LiveSession? = guard.withLock {
        val existing = current
        if (!DuplicateSessionRule.accepts(existing?.origin, origin, role)) return@withLock null
        existing?.shutDown(CloseReason.DUPLICATE_SESSION)
        val link = SessionPeerLink(peerDeviceId, session)
        val claimed = LiveSession(origin, session, link, routers.create(selfDeviceId, link, settings))
        current = claimed
        liveState.value = true
        claimed
    }

    suspend fun release(session: PeerSession): Boolean = guard.withLock {
        val existing = current
        if (existing == null || existing.session !== session) return@withLock false
        existing.router.cancel()
        current = null
        liveState.value = false
        true
    }

    suspend fun close(reason: CloseReason) {
        guard.withLock {
            current?.shutDown(reason)
            current = null
            liveState.value = false
        }
    }

    suspend fun sendCaps() {
        val link = current?.link ?: return
        quietly { link.send(settings.policy.toCaps()) }
    }

    suspend fun sendPolicy(directions: PeerDirections, policy: SyncPolicy) {
        val link = current?.link ?: return
        quietly {
            link.send(
                PolicySync(
                    revision = directions.revision,
                    send = directions.send,
                    receive = directions.receive,
                    paused = policy.paused,
                    maxBlobBytes = policy.maxBlobBytes,
                ),
            )
        }
    }
}
