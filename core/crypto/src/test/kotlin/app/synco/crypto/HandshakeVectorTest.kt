package app.synco.crypto

import app.synco.protocol.encoding.Hex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HandshakeVectorTest {

    private val alice = IdentityKeyPair.fromPrivateKey(Hex.decode(STATIC_A))
    private val bob = IdentityKeyPair.fromPrivateKey(Hex.decode(STATIC_B))
    private val aliceEphemeral = EphemeralKeyPair.fromPrivateKey(Hex.decode(EPHEMERAL_A))
    private val bobEphemeral = EphemeralKeyPair.fromPrivateKey(Hex.decode(EPHEMERAL_B))

    @Test
    fun `derives the pinned device ids from the pinned static keys`() {
        assertEquals("gagjzfqdxevewopn", alice.deviceId.value)
        assertEquals("6npfmfqwbiyl6pdo", bob.deviceId.value)
        assertEquals(HandshakeRole.INITIATOR, HandshakeRole.of(bob.deviceId, alice.deviceId))
    }

    @Test
    fun `derives the pinned directional session keys`() {
        assertEquals(INITIATOR_TO_RESPONDER_KEY, Hex.encodeLower(initiator().keys.sendKey))
        assertEquals(RESPONDER_TO_INITIATOR_KEY, Hex.encodeLower(initiator().keys.receiveKey))
        assertEquals(RESPONDER_TO_INITIATOR_KEY, Hex.encodeLower(responder().keys.sendKey))
        assertEquals(INITIATOR_TO_RESPONDER_KEY, Hex.encodeLower(responder().keys.receiveKey))
    }

    @Test
    fun `derives the pinned confirmation tags`() {
        assertEquals(INITIATOR_TAG, Hex.encodeLower(initiator().confirmationTag))
        assertEquals(RESPONDER_TAG, Hex.encodeLower(responder().confirmationTag))
        assertTrue(responder().verifyPeerTag(initiator().confirmationTag))
        assertTrue(initiator().verifyPeerTag(responder().confirmationTag))
    }

    private fun initiator(): HandshakeResult = Handshake(
        role = HandshakeRole.INITIATOR,
        identity = bob,
        ephemeral = bobEphemeral,
        peerStaticPublicKey = alice.publicKey,
        peerEphemeralPublicKey = aliceEphemeral.publicKey,
        selfDeviceId = bob.deviceId,
        peerDeviceId = alice.deviceId,
    ).derive()

    private fun responder(): HandshakeResult = Handshake(
        role = HandshakeRole.RESPONDER,
        identity = alice,
        ephemeral = aliceEphemeral,
        peerStaticPublicKey = bob.publicKey,
        peerEphemeralPublicKey = bobEphemeral.publicKey,
        selfDeviceId = alice.deviceId,
        peerDeviceId = bob.deviceId,
    ).derive()

    private companion object {
        const val STATIC_A = "77076d0a7318a57d3c16c17251b26645df4c2f87ebc0992ab177fba51db92c2a"
        const val STATIC_B = "5dab087e624a8a4b79e17f8b83800ee66f3bb1292618b6fd1c2f8b27ff88e0eb"
        const val EPHEMERAL_A = "0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20"
        const val EPHEMERAL_B = "2122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f40"

        const val INITIATOR_TO_RESPONDER_KEY =
            "2755145f7862aafd7487ef259d0488f642c0f9511ec87d7a00444c3481c39dc0"
        const val RESPONDER_TO_INITIATOR_KEY =
            "824843b53257a8b6211954dc4c36a6a347bd7524cf169e345da6ce48891672c9"
        const val INITIATOR_TAG =
            "92693d44b8915eac4e3b628be4c2ba0fc4f9e94828cff4afd699bc71a00cfeaa"
        const val RESPONDER_TAG =
            "1ac578ed8be927939a0a47319db29fd622bfe68786b1dbab1c287f61f40754a1"
    }
}
