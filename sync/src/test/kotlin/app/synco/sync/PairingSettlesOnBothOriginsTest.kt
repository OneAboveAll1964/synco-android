package app.synco.sync

import app.synco.protocol.DeviceId
import app.synco.protocol.Platform
import app.synco.transport.PairingResult
import app.synco.transport.PeerDescriptor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PairingSettlesOnBothOriginsTest {

    private val peer = PeerDescriptor(
        deviceId = DeviceId("pxukohdvrdyew5ew"),
        displayName = "Shko s MacBook Pro",
        platform = Platform.MACOS,
        staticPublicKey = ByteArray(32) { 7 },
    )

    @Test
    fun `a dialed pairing is stored, not only an accepted one`() = runTest {
        val store = RecordingTrustedPeers()
        val coordinator = PairingCoordinator(store, RecordingEvents())
        coordinator.approve(peer)

        coordinator.settle(PairingResult.Approved(peer))

        val stored = store.peers.first()
        assertEquals(listOf(peer.deviceId), stored.map { it.deviceId })
        assertTrue(stored.single().isTrusted)
    }

    @Test
    fun `a rejection is stored so the peer stops being asked about`() = runTest {
        val store = RecordingTrustedPeers()
        val coordinator = PairingCoordinator(store, RecordingEvents())
        coordinator.approve(peer)

        coordinator.settle(PairingResult.DeclinedLocally(peer.deviceId))

        assertTrue(store.peers.first().single().rejected)
    }

    @Test
    fun `an unremembered peer settles to nothing rather than storing a blank record`() = runTest {
        val store = RecordingTrustedPeers()
        val coordinator = PairingCoordinator(store, RecordingEvents())

        assertEquals(null, coordinator.settle(PairingResult.Approved(peer)))
        assertTrue(store.peers.first().isEmpty())
    }
}
