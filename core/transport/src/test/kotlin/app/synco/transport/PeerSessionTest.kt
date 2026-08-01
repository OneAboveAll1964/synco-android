package app.synco.transport

import app.synco.protocol.Platform
import app.synco.protocol.message.Caps
import app.synco.protocol.message.CapsFlags
import app.synco.protocol.message.CloseReason
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PeerSessionTest {

    @Test
    fun `paired peers establish an encrypted session and exchange control messages`() = runTest {
        val link = TestLink()
        val alice = SessionFixtures.device("Alice")
        val bob = SessionFixtures.device("Bob", Platform.MACOS)
        val alices = session(alice, link.left, SessionFixtures.trusting(bob))
        val bobs = session(bob, link.right, SessionFixtures.trusting(alice))

        val alicesRun = async { alices.run() }
        val bobsRun = async { bobs.run() }

        val greeting = bobs.events.first()
        assertTrue(greeting is SessionEvent.Established)
        assertEquals(alice.deviceId, (greeting as SessionEvent.Established).peer.deviceId)
        assertEquals(alice.fingerprint, greeting.peer.fingerprint)
        assertEquals(Platform.ANDROID, greeting.peer.platform)
        assertTrue(link.right.isEncrypted)

        val caps = Caps(CapsFlags.ALL_ENABLED, CapsFlags.ALL_DISABLED)
        alices.send(caps)
        assertEquals(SessionEvent.Received(caps), bobs.events.first())

        alices.close(CloseReason.SHUTDOWN)
        assertEquals(CloseReason.SHUTDOWN, bobsRun.await().closeReason)
        assertEquals(CloseReason.SHUTDOWN, alicesRun.await().closeReason)
        assertEquals(bob.deviceId, alices.peer?.deviceId)
    }

    @Test
    fun `an unpaired peer runs the plaintext pairing exchange instead`() = runTest {
        val link = TestLink()
        val alice = SessionFixtures.device("Alice")
        val bob = SessionFixtures.device("Bob", Platform.MACOS)
        val alices = session(alice, link.left, SessionFixtures.untrusting())
        val bobs = session(bob, link.right, SessionFixtures.untrusting())

        val alicesRun = async { alices.run() }
        val bobsRun = async { bobs.run() }

        val alicesOutcome = alicesRun.await()
        val bobsOutcome = bobsRun.await()
        assertEquals(CloseReason.UNPAIRED, alicesOutcome.closeReason)
        assertFalse(link.left.isEncrypted)
        assertEquals(bob.deviceId, pairedPeer(alicesOutcome).deviceId)
        assertEquals(bob.fingerprint, pairedPeer(alicesOutcome).fingerprint)
        assertEquals(alice.deviceId, pairedPeer(bobsOutcome).deviceId)
    }

    @Test
    fun `a peer that was forgotten by the other side pairs again instead of stalling`() = runTest {
        val link = TestLink()
        val alice = SessionFixtures.device("Alice")
        val bob = SessionFixtures.device("Bob", Platform.MACOS)
        val alices = session(alice, link.left, SessionFixtures.trusting(bob))
        val bobs = session(bob, link.right, SessionFixtures.untrusting())

        val alicesRun = async { alices.run() }
        val bobsRun = async { bobs.run() }

        val alicesOutcome = alicesRun.await()
        val bobsOutcome = bobsRun.await()
        assertEquals(bob.deviceId, pairedPeer(alicesOutcome).deviceId)
        assertEquals(alice.deviceId, pairedPeer(bobsOutcome).deviceId)
        assertFalse(link.left.isEncrypted)
    }

    @Test
    fun `the forgetful side pairing again works whichever end forgot`() = runTest {
        val link = TestLink()
        val alice = SessionFixtures.device("Alice")
        val bob = SessionFixtures.device("Bob", Platform.MACOS)
        val alices = session(alice, link.left, SessionFixtures.untrusting())
        val bobs = session(bob, link.right, SessionFixtures.trusting(alice))

        val alicesRun = async { alices.run() }
        val bobsRun = async { bobs.run() }

        assertEquals(bob.deviceId, pairedPeer(alicesRun.await()).deviceId)
        assertEquals(alice.deviceId, pairedPeer(bobsRun.await()).deviceId)
    }

    @Test
    fun `a declining peer ends the pairing exchange without trust`() = runTest {
        val link = TestLink()
        val alice = SessionFixtures.device("Alice")
        val bob = SessionFixtures.device("Bob", Platform.MACOS)
        val alices = session(alice, link.left, SessionFixtures.untrusting(), approved = true)
        val bobs = session(bob, link.right, SessionFixtures.untrusting(), approved = false)

        val alicesRun = async { alices.run() }
        val bobsRun = async { bobs.run() }

        val alicesResult = (alicesRun.await() as SessionOutcome.Pairing).result
        val bobsResult = (bobsRun.await() as SessionOutcome.Pairing).result
        assertEquals(PairingResult.DeclinedByPeer(bob.deviceId), alicesResult)
        assertEquals(PairingResult.DeclinedLocally(alice.deviceId), bobsResult)
    }

    private fun session(
        local: LocalDevice,
        frames: FramedConnection,
        trustedPeers: TrustedPeers,
        approved: Boolean = true,
    ): PeerSession = PeerSession(local, frames, trustedPeers, SessionFixtures.approval(approved))

    private fun pairedPeer(outcome: SessionOutcome): PeerDescriptor =
        ((outcome as SessionOutcome.Pairing).result as PairingResult.Approved).peer
}
