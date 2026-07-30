package app.synco.crypto

import app.synco.protocol.encoding.Hex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SharedHandshakeVectorTest {

    @Test
    fun `derives the shared public keys and device ids`() {
        forEachVector { vector, parties ->
            assertEquals(vector.name, vector.staticPublicA, Hex.encodeLower(parties.identityA.publicKey))
            assertEquals(vector.name, vector.staticPublicB, Hex.encodeLower(parties.identityB.publicKey))
            assertEquals(vector.name, vector.ephemeralPublicA, Hex.encodeLower(parties.ephemeralA.publicKey))
            assertEquals(vector.name, vector.ephemeralPublicB, Hex.encodeLower(parties.ephemeralB.publicKey))
            assertEquals(vector.name, vector.deviceIdA, parties.identityA.deviceId.value)
            assertEquals(vector.name, vector.deviceIdB, parties.identityB.deviceId.value)
        }
    }

    @Test
    fun `agrees on which side initiates`() {
        forEachVector { vector, parties ->
            assertEquals(
                vector.name,
                HandshakeRole.INITIATOR,
                HandshakeRole.of(parties.identityB.deviceId, parties.identityA.deviceId),
            )
            assertEquals(
                vector.name,
                HandshakeRole.RESPONDER,
                HandshakeRole.of(parties.identityA.deviceId, parties.identityB.deviceId),
            )
        }
    }

    @Test
    fun `derives the shared directional session keys`() {
        forEachVector { vector, parties ->
            val initiator = parties.initiator()
            val responder = parties.responder()
            assertEquals(vector.name, vector.initiatorToResponderKey, Hex.encodeLower(initiator.keys.sendKey))
            assertEquals(vector.name, vector.responderToInitiatorKey, Hex.encodeLower(initiator.keys.receiveKey))
            assertEquals(vector.name, vector.responderToInitiatorKey, Hex.encodeLower(responder.keys.sendKey))
            assertEquals(vector.name, vector.initiatorToResponderKey, Hex.encodeLower(responder.keys.receiveKey))
        }
    }

    @Test
    fun `derives the shared confirmation tags`() {
        forEachVector { vector, parties ->
            val initiator = parties.initiator()
            val responder = parties.responder()
            assertEquals(vector.name, vector.initiatorTag, Hex.encodeLower(initiator.confirmationTag))
            assertEquals(vector.name, vector.responderTag, Hex.encodeLower(responder.confirmationTag))
            assertTrue(vector.name, responder.verifyPeerTag(initiator.confirmationTag))
            assertTrue(vector.name, initiator.verifyPeerTag(responder.confirmationTag))
        }
    }

    private fun forEachVector(assertion: (SharedHandshakeVector, Parties) -> Unit) {
        SHARED_HANDSHAKE_VECTORS.forEach { vector -> assertion(vector, Parties(vector)) }
    }

    private class Parties(vector: SharedHandshakeVector) {
        val identityA = IdentityKeyPair.fromPrivateKey(Hex.decode(vector.staticPrivateA))
        val identityB = IdentityKeyPair.fromPrivateKey(Hex.decode(vector.staticPrivateB))
        val ephemeralA = EphemeralKeyPair.fromPrivateKey(Hex.decode(vector.ephemeralPrivateA))
        val ephemeralB = EphemeralKeyPair.fromPrivateKey(Hex.decode(vector.ephemeralPrivateB))

        fun initiator(): HandshakeResult = Handshake(
            role = HandshakeRole.INITIATOR,
            identity = identityB,
            ephemeral = ephemeralB,
            peerStaticPublicKey = identityA.publicKey,
            peerEphemeralPublicKey = ephemeralA.publicKey,
            selfDeviceId = identityB.deviceId,
            peerDeviceId = identityA.deviceId,
        ).derive()

        fun responder(): HandshakeResult = Handshake(
            role = HandshakeRole.RESPONDER,
            identity = identityA,
            ephemeral = ephemeralA,
            peerStaticPublicKey = identityB.publicKey,
            peerEphemeralPublicKey = ephemeralB.publicKey,
            selfDeviceId = identityA.deviceId,
            peerDeviceId = identityB.deviceId,
        ).derive()
    }
}
