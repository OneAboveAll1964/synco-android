package app.synco.crypto

import app.synco.protocol.SyncoError
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class HandshakeTest {

    @Test
    fun `both sides derive the same directional keys`() {
        val (initiator, responder) = derivedPair()
        assertArrayEquals(initiator.keys.sendKey, responder.keys.receiveKey)
        assertArrayEquals(initiator.keys.receiveKey, responder.keys.sendKey)
        assertFalse(initiator.keys.sendKey.contentEquals(initiator.keys.receiveKey))
    }

    @Test
    fun `each side verifies the confirmation tag of the other`() {
        val (initiator, responder) = derivedPair()
        assertTrue(responder.verifyPeerTag(initiator.confirmationTag))
        assertTrue(initiator.verifyPeerTag(responder.confirmationTag))
        initiator.requirePeerTag(responder.confirmationTag)
        responder.requirePeerTag(initiator.confirmationTag)
    }

    @Test
    fun `binds each tag to its own direction key and device id`() {
        val (initiator, responder) = derivedPair()
        assertFalse(initiator.verifyPeerTag(initiator.confirmationTag))
        assertFalse(responder.verifyPeerTag(responder.confirmationTag))
    }

    @Test
    fun `rejects a tampered tag`() {
        val (initiator, responder) = derivedPair()
        val tampered = initiator.confirmationTag.also { it[0] = (it[0] + 1).toByte() }
        assertFalse(responder.verifyPeerTag(tampered))
        assertThrows(SyncoError.BadAuth::class.java) { responder.requirePeerTag(tampered) }
        assertThrows(SyncoError.BadAuth::class.java) { responder.requirePeerTag(ByteArray(0)) }
    }

    @Test
    fun `derives a fresh session for every connection`() {
        assertFalse(derivedPair().first.keys.sendKey.contentEquals(derivedPair().first.keys.sendKey))
    }

    @Test
    fun `hands the derived keys to a matched pair of ciphers`() {
        val (initiator, responder) = derivedPair()
        val payload = "session payload".toByteArray()
        assertArrayEquals(payload, responder.ciphers().open(initiator.ciphers().seal(payload)))
    }

    @Test
    fun `rejects a role that contradicts the device id ordering`() {
        val alice = IdentityKeyPair.generate()
        val bob = IdentityKeyPair.generate()
        val wrongRole = when (HandshakeRole.of(alice.deviceId, bob.deviceId)) {
            HandshakeRole.INITIATOR -> HandshakeRole.RESPONDER
            HandshakeRole.RESPONDER -> HandshakeRole.INITIATOR
        }
        assertThrows(IllegalArgumentException::class.java) {
            handshake(wrongRole, alice, bob)
        }
    }

    @Test
    fun `rejects a handshake with itself`() {
        val alice = IdentityKeyPair.generate()
        assertThrows(IllegalArgumentException::class.java) {
            Handshake(
                role = HandshakeRole.INITIATOR,
                identity = alice,
                ephemeral = EphemeralKeyPair.generate(),
                peerStaticPublicKey = alice.publicKey,
                peerEphemeralPublicKey = EphemeralKeyPair.generate().publicKey,
                selfDeviceId = alice.deviceId,
                peerDeviceId = alice.deviceId,
            )
        }
    }

    @Test
    fun `rejects a peer key of the wrong length`() {
        val alice = IdentityKeyPair.generate()
        val bob = IdentityKeyPair.generate()
        assertThrows(IllegalArgumentException::class.java) {
            Handshake(
                role = HandshakeRole.of(alice.deviceId, bob.deviceId),
                identity = alice,
                ephemeral = EphemeralKeyPair.generate(),
                peerStaticPublicKey = ByteArray(31),
                peerEphemeralPublicKey = EphemeralKeyPair.generate().publicKey,
                selfDeviceId = alice.deviceId,
                peerDeviceId = bob.deviceId,
            ).derive()
        }
    }

    private fun derivedPair(): Pair<HandshakeResult, HandshakeResult> {
        val alice = IdentityKeyPair.generate()
        val bob = IdentityKeyPair.generate()
        val aliceEphemeral = EphemeralKeyPair.generate()
        val bobEphemeral = EphemeralKeyPair.generate()
        val aliceRole = HandshakeRole.of(alice.deviceId, bob.deviceId)
        val aliceResult = handshake(aliceRole, alice, bob, aliceEphemeral, bobEphemeral).derive()
        val bobResult = handshake(
            HandshakeRole.of(bob.deviceId, alice.deviceId),
            bob,
            alice,
            bobEphemeral,
            aliceEphemeral,
        ).derive()
        return if (aliceRole == HandshakeRole.INITIATOR) aliceResult to bobResult else bobResult to aliceResult
    }

    private fun handshake(
        role: HandshakeRole,
        self: IdentityKeyPair,
        peer: IdentityKeyPair,
        selfEphemeral: EphemeralKeyPair = EphemeralKeyPair.generate(),
        peerEphemeral: EphemeralKeyPair = EphemeralKeyPair.generate(),
    ): Handshake = Handshake(
        role = role,
        identity = self,
        ephemeral = selfEphemeral,
        peerStaticPublicKey = peer.publicKey,
        peerEphemeralPublicKey = peerEphemeral.publicKey,
        selfDeviceId = self.deviceId,
        peerDeviceId = peer.deviceId,
    )
}
