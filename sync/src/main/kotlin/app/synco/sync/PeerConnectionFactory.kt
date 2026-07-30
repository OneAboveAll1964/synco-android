package app.synco.sync

import app.synco.protocol.DeviceId
import kotlinx.coroutines.CoroutineScope

internal class PeerConnectionFactory(
    private val selfDeviceId: DeviceId,
    private val policies: PolicyBook,
    private val routers: ClipRouterFactory,
    private val dialer: PeerDialer,
    private val runner: PeerSessionRunner,
    private val events: SyncEventSink,
    private val scope: CoroutineScope,
) {
    fun create(peerDeviceId: DeviceId): PeerConnection {
        val role = DialRule.roleFor(selfDeviceId, peerDeviceId)
        val settings = PeerSettings(policies.policyFor(peerDeviceId))
        return PeerConnection(
            peerDeviceId = peerDeviceId,
            role = role,
            settings = settings,
            slot = PeerSessionSlot(selfDeviceId, peerDeviceId, role, settings, routers),
            dialer = dialer,
            runner = runner,
            events = events,
            scope = scope,
        )
    }
}
