package app.synco.sync

import app.synco.discovery.DiscoveredPeer
import app.synco.protocol.DeviceId
import app.synco.protocol.Fingerprint
import app.synco.protocol.Platform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class PeerSessionRegistryTest {

    private val self = DeviceId("spmf5yhlkmism7vs")
    private val mac = DeviceId("kbhhxmb5effwrego")
    private val dialer = IdlePeerDialer()

    @Test
    fun `a discovered peer bearing our own device id never reaches the peer list`() = runTest {
        val registry = registryIn(backgroundScope)

        registry.applyDiscovered(listOf(discovered(self, displayName = "SM-S936B")))

        assertTrue(registry.views.first().isEmpty())
        assertTrue(dialer.attempts.isEmpty())
    }

    @Test
    fun `a discovered peer bearing another device id reaches the peer list`() = runTest {
        val registry = registryIn(backgroundScope)

        registry.applyDiscovered(listOf(discovered(mac, displayName = "Shko s MacBook Pro")))

        assertEquals(listOf(mac), registry.views.first().map { it.deviceId })
    }

    @Test
    fun `an advertisement of our own device id is filtered out of a mixed browse`() = runTest {
        val registry = registryIn(backgroundScope)

        registry.applyDiscovered(
            listOf(
                discovered(self, displayName = "SM-S936B"),
                discovered(mac, displayName = "Shko s MacBook Pro"),
            ),
        )

        assertEquals(listOf(mac), registry.views.first().map { it.deviceId })
    }

    private fun registryIn(scope: CoroutineScope): PeerSessionRegistry {
        val events = RecordingEvents()
        return PeerSessionRegistry(
            selfDeviceId = self,
            routers = ClipRouterFactory(
                clipboard = RecordingClipboard(),
                transfers = StubTransferGateway(),
                blobs = StubBlobSender(),
                events = events,
                destination = NoReceivedFileDestination(),
                announcer = RecordingAnnouncer(),
            ),
            dialer = dialer,
            events = events,
            scope = scope,
            pairings = PairingCoordinator(RecordingTrustedPeers(), events),
        )
    }

    private fun discovered(deviceId: DeviceId, displayName: String): DiscoveredPeer = DiscoveredPeer(
        deviceId = deviceId,
        displayName = displayName,
        platform = Platform.MACOS,
        fingerprint = Fingerprint("A1B2-C3D4-E5F6-0718"),
        host = "192.168.1.10",
        port = 47_600,
        lastSeen = Instant.parse("2026-07-30T16:55:00Z"),
    )
}
