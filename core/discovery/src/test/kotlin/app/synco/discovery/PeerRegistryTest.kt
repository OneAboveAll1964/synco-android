package app.synco.discovery

import app.synco.protocol.DeviceId
import app.synco.protocol.Fingerprint
import app.synco.protocol.Platform
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PeerRegistryTest {

    private val registry = PeerRegistry()
    private val self = DeviceId("spmf5yhlkmism7vs")
    private val now: Instant = Instant.parse("2026-07-30T16:55:00Z")

    @Test
    fun `keeps one entry per device id`() {
        appear(peer("abcdefghij234567", port = 1000))
        appear(peer("abcdefghij234567", port = 2000))

        assertEquals(1, registry.peers.value.size)
        assertEquals(2000, registry.peers.value.single().port)
    }

    @Test
    fun `drops a peer that disappears`() {
        appear(peer("abcdefghij234567", port = 1000))
        appear(peer("bbcdefghij234567", port = 1001))
        registry.record(PeerEvent.Disappeared(DeviceId("abcdefghij234567")), self)

        assertEquals(listOf(DeviceId("bbcdefghij234567")), registry.peers.value.map { it.deviceId })
    }

    @Test
    fun `orders peers by display name`() {
        appear(peer("abcdefghij234567", port = 1, displayName = "Zoe"))
        appear(peer("bbcdefghij234567", port = 2, displayName = "Ada"))

        assertEquals(listOf("Ada", "Zoe"), registry.peers.value.map { it.displayName })
    }

    @Test
    fun `clears every peer`() {
        appear(peer("abcdefghij234567", port = 1))
        registry.clear()

        assertTrue(registry.peers.value.isEmpty())
    }

    @Test
    fun `never publishes an advertisement carrying our own device id`() {
        appear(peer(self.value, port = 1, displayName = "SM-S936B"))

        assertTrue(registry.peers.value.isEmpty())
    }

    @Test
    fun `drops a peer unseen for longer than the expiry window`() {
        appear(peer("abcdefghij234567", port = 1, lastSeen = now))

        registry.dropExpired(now.plusMillis(DiscoveryTuning.PEER_EXPIRY_MILLIS + 1))

        assertTrue(registry.peers.value.isEmpty())
    }

    @Test
    fun `keeps a peer seen within the expiry window`() {
        appear(peer("abcdefghij234567", port = 1, lastSeen = now))

        registry.dropExpired(now.plusMillis(DiscoveryTuning.PEER_EXPIRY_MILLIS - 1))

        assertEquals(1, registry.peers.value.size)
    }

    @Test
    fun `a peer seen again survives the sweep that would have expired it`() {
        appear(peer("abcdefghij234567", port = 1, lastSeen = now))
        val refreshedAt = now.plusMillis(DiscoveryTuning.BROWSE_REFRESH_MILLIS)
        appear(peer("abcdefghij234567", port = 1, lastSeen = refreshedAt))

        registry.dropExpired(now.plusMillis(DiscoveryTuning.PEER_EXPIRY_MILLIS + 1))

        assertEquals(listOf(refreshedAt), registry.peers.value.map { it.lastSeen })
    }

    @Test
    fun `expires every peer once a whole network vanishes without goodbyes`() {
        appear(peer("abcdefghij234567", port = 1, lastSeen = now))
        appear(peer("bbcdefghij234567", port = 2, lastSeen = now))

        registry.dropExpired(now.plusMillis(DiscoveryTuning.PEER_EXPIRY_MILLIS + 1))

        assertTrue(registry.peers.value.isEmpty())
    }

    private fun appear(peer: DiscoveredPeer) {
        registry.record(PeerEvent.Appeared(peer), self)
    }

    private fun peer(
        deviceId: String,
        port: Int,
        displayName: String = "Peer",
        lastSeen: Instant = Instant.EPOCH,
    ): DiscoveredPeer = DiscoveredPeer(
        deviceId = DeviceId(deviceId),
        displayName = displayName,
        platform = Platform.MACOS,
        fingerprint = Fingerprint("A1B2-C3D4-E5F6-0718"),
        host = "192.168.1.10",
        port = port,
        lastSeen = lastSeen,
    )
}
