package app.synco.sync

import app.synco.crypto.HandshakeRole
import app.synco.protocol.DeviceId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionRoleTest {

    private val smaller = DeviceId("abcdefghij234567")
    private val larger = DeviceId("abcdefghij234577")

    @Test
    fun `the lexicographically smaller device id dials and the larger one waits`() {
        assertEquals(HandshakeRole.INITIATOR, DialRule.roleFor(smaller, larger))
        assertEquals(HandshakeRole.RESPONDER, DialRule.roleFor(larger, smaller))
        assertEquals(PeerIntent.DIAL, intentOf(smaller, larger))
        assertEquals(PeerIntent.WAIT, intentOf(larger, smaller))
    }

    @Test
    fun `nothing is dialled for an undiscovered rejected or already live peer`() {
        val dialing = DialRule.roleFor(smaller, larger)
        assertEquals(PeerIntent.IDLE, DialRule.intentOf(dialing, discovered = false, rejected = false, live = false))
        assertEquals(PeerIntent.IDLE, DialRule.intentOf(dialing, discovered = true, rejected = true, live = false))
        assertEquals(PeerIntent.IDLE, DialRule.intentOf(dialing, discovered = true, rejected = false, live = true))
        val waiting = DialRule.roleFor(larger, smaller)
        assertEquals(PeerIntent.IDLE, DialRule.intentOf(waiting, discovered = true, rejected = false, live = true))
    }

    @Test
    fun `the first session for a peer is always kept`() {
        assertTrue(DuplicateSessionRule.accepts(null, SessionOrigin.DIALED, HandshakeRole.INITIATOR))
        assertTrue(DuplicateSessionRule.accepts(null, SessionOrigin.ACCEPTED, HandshakeRole.INITIATOR))
        assertTrue(DuplicateSessionRule.accepts(null, SessionOrigin.DIALED, HandshakeRole.RESPONDER))
        assertTrue(DuplicateSessionRule.accepts(null, SessionOrigin.ACCEPTED, HandshakeRole.RESPONDER))
    }

    @Test
    fun `a duplicate resolves in favour of the session whose initiator role is right`() {
        assertTrue(
            DuplicateSessionRule.accepts(SessionOrigin.ACCEPTED, SessionOrigin.DIALED, HandshakeRole.INITIATOR),
        )
        assertFalse(
            DuplicateSessionRule.accepts(SessionOrigin.DIALED, SessionOrigin.ACCEPTED, HandshakeRole.INITIATOR),
        )
        assertTrue(
            DuplicateSessionRule.accepts(SessionOrigin.DIALED, SessionOrigin.ACCEPTED, HandshakeRole.RESPONDER),
        )
        assertFalse(
            DuplicateSessionRule.accepts(SessionOrigin.ACCEPTED, SessionOrigin.DIALED, HandshakeRole.RESPONDER),
        )
    }

    @Test
    fun `a second session of the same origin is refused`() {
        assertFalse(
            DuplicateSessionRule.accepts(SessionOrigin.DIALED, SessionOrigin.DIALED, HandshakeRole.INITIATOR),
        )
        assertFalse(
            DuplicateSessionRule.accepts(SessionOrigin.ACCEPTED, SessionOrigin.ACCEPTED, HandshakeRole.RESPONDER),
        )
    }

    private fun intentOf(selfDeviceId: DeviceId, peerDeviceId: DeviceId): PeerIntent = DialRule.intentOf(
        role = DialRule.roleFor(selfDeviceId, peerDeviceId),
        discovered = true,
        rejected = false,
        live = false,
    )
}
