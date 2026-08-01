package app.synco.sync

import app.synco.logging.SyncoLog
import app.synco.protocol.DeviceId
import app.synco.protocol.message.PolicySync
import app.synco.storage.PeerDirections
import app.synco.storage.SettingsStore

internal class PolicyExchange(
    private val selfDeviceId: DeviceId,
    private val settings: SettingsStore,
) {
    suspend fun localPolicy(peerDeviceId: DeviceId): PeerDirections =
        settings.directionsFor(peerDeviceId)

    suspend fun adopt(peerDeviceId: DeviceId, incoming: PolicySync): Boolean {
        val mirrored = incoming.mirrored()
        val candidate = PeerDirections(
            send = mirrored.send,
            receive = mirrored.receive,
            revision = incoming.revision,
        )
        val current = settings.directionsFor(peerDeviceId)
        if (!candidate.supersedes(current, selfDeviceId.value, peerDeviceId.value)) {
            SyncoLog.engine.debug { "ignored an older policy from $peerDeviceId" }
            return false
        }
        settings.adoptDirections(peerDeviceId, candidate)
        settings.setPaused(incoming.paused)
        settings.setMaxBlobBytes(incoming.maxBlobBytes)
        SyncoLog.engine.info("adopted the policy $peerDeviceId sent")
        return true
    }
}
