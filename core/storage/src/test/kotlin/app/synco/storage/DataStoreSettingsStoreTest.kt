package app.synco.storage

import app.synco.protocol.message.CapsFlags
import app.synco.storage.TrustedPeerFixtures.MAC
import app.synco.storage.TrustedPeerFixtures.PHANTOM
import app.synco.storage.TrustedPeerFixtures.PHONE
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DataStoreSettingsStoreTest {

    private val preferences = InMemoryPreferences()
    private val settings =
        DataStoreSettingsStore(preferences, fallbackDisplayName = "SM-S936B", clock = { EDITED_AT })
    private val trustedPeers = DataStoreTrustedPeerStore(preferences)

    @Test
    fun `refuses to persist a policy for a device id that was never paired`() = runTest {
        settings.setDirections(PHANTOM, PeerDirections.NONE)

        assertTrue(storedDirections().isEmpty())
        assertTrue(settings.policies.first().isEmpty())
    }

    @Test
    fun `persists a policy for a paired device id`() = runTest {
        trustedPeers.add(TrustedPeerFixtures.peer(MAC))

        settings.setDirections(MAC, PeerDirections.NONE)

        assertEquals(mapOf(MAC.value to edited(PeerDirections.NONE)), storedDirections())
        assertEquals(edited(PeerDirections.NONE), settings.policy(MAC).first().directions)
    }

    @Test
    fun `refuses a policy for a device id that is only being proposed for pairing`() = runTest {
        trustedPeers.add(TrustedPeerFixtures.peer(MAC))
        settings.setDirections(MAC, PeerDirections.NONE)

        settings.setDirections(PHONE, PeerDirections.NONE)

        assertEquals(setOf(MAC.value), storedDirections().keys)
        assertFalse(PHONE in settings.policies.first())
    }

    @Test
    fun `prunes a policy row whose device id is in no trusted record`() = runTest {
        trustedPeers.add(TrustedPeerFixtures.peer(MAC))
        settings.setDirections(MAC, PeerDirections.NONE)
        seedOrphan()

        settings.pruneUntrustedDirections()

        assertEquals(setOf(MAC.value), storedDirections().keys)
    }

    @Test
    fun `keeps the policy of a trusted peer that is merely offline`() = runTest {
        trustedPeers.add(TrustedPeerFixtures.peer(MAC))
        val outboundOnly = PeerDirections(
            send = CapsFlags.ALL_ENABLED,
            receive = CapsFlags.ALL_DISABLED,
        )
        settings.setDirections(MAC, outboundOnly)

        settings.pruneUntrustedDirections()

        assertEquals(mapOf(MAC.value to edited(outboundOnly)), storedDirections())
    }

    @Test
    fun `keeps the policy of a peer whose pairing was rejected`() = runTest {
        trustedPeers.add(TrustedPeerFixtures.peer(MAC))
        settings.setDirections(MAC, PeerDirections.NONE)
        trustedPeers.setRejected(MAC, rejected = true)

        settings.pruneUntrustedDirections()

        assertEquals(setOf(MAC.value), storedDirections().keys)
    }

    @Test
    fun `never reports a policy for an untrusted device id`() = runTest {
        seedOrphan()

        assertTrue(settings.policies.first().isEmpty())
        assertEquals(
            settings.defaultPolicy.first().directions,
            settings.policy(PHANTOM).first().directions,
        )
    }

    @Test
    fun `keeps every policy when the trusted peer record cannot be decoded`() = runTest {
        trustedPeers.add(TrustedPeerFixtures.peer(MAC))
        settings.setDirections(MAC, PeerDirections.NONE)
        corruptTrustedPeers()

        settings.pruneUntrustedDirections()

        assertEquals(setOf(MAC.value), storedDirections().keys)
    }

    @Test
    fun `refuses a new policy while the trusted peer record cannot be decoded`() = runTest {
        corruptTrustedPeers()

        settings.setDirections(MAC, PeerDirections.NONE)

        assertTrue(storedDirections().isEmpty())
    }

    @Test
    fun `forgetting a peer clears its policy after the trust record is gone`() = runTest {
        trustedPeers.add(TrustedPeerFixtures.peer(MAC))
        settings.setDirections(MAC, PeerDirections.NONE)

        trustedPeers.remove(MAC)
        settings.clearDirections(MAC)

        assertTrue(storedDirections().isEmpty())
    }

    private suspend fun seedOrphan() {
        preferences.updateData { stored ->
            val orphan = StoredDirections.all(stored) + (PHANTOM.value to PeerDirections.NONE)
            stored.toMutablePreferences().apply {
                this[StorageKeys.PEER_DIRECTIONS] =
                    StorageJson.encodeToString(DirectionsMapSerializer, orphan)
            }
        }
    }

    private suspend fun corruptTrustedPeers() {
        preferences.updateData { stored ->
            stored.toMutablePreferences().apply {
                this[StorageKeys.TRUSTED_PEERS] = "{\"$MAC\":{\"did\""
            }
        }
    }

    private suspend fun storedDirections(): Map<String, PeerDirections> =
        StoredDirections.all(preferences.data.first())

    private fun edited(directions: PeerDirections): PeerDirections =
        directions.copy(revision = EDITED_AT)

    private companion object {
        const val EDITED_AT = 1_700_000_000_000L
    }
}
