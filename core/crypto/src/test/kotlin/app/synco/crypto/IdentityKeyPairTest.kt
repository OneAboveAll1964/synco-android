package app.synco.crypto

import app.synco.protocol.DeviceId
import app.synco.protocol.HandshakeConstants
import app.synco.protocol.ProtocolConstants
import app.synco.protocol.encoding.Hex
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IdentityKeyPairTest {

    @Test
    fun `derives the public key from a restored private key`() {
        val identity = IdentityKeyPair.fromPrivateKey(Hex.decode(ALICE_PRIVATE))
        assertArrayEquals(Hex.decode(ALICE_PUBLIC), identity.publicKey)
    }

    @Test
    fun `derives the device id and fingerprint from the static public key hash`() {
        val identity = IdentityKeyPair.fromPrivateKey(Hex.decode(ALICE_PRIVATE))
        assertEquals("gagjzfqdxevewopn", identity.deviceId.value)
        assertEquals("300C-9C96-03B9-2A4B", identity.fingerprint.grouped)
        assertEquals("300C9C9603B92A4B", identity.fingerprint.plain)
    }

    @Test
    fun `agrees with the peer derivation of the same identity`() {
        val identity = IdentityKeyPair.fromPrivateKey(Hex.decode(ALICE_PRIVATE))
        assertEquals(identity.deviceId, PeerIdentity.deviceIdOf(identity.publicKey))
        assertEquals(identity.fingerprint, PeerIdentity.fingerprintOf(identity.publicKey))
        assertTrue(PeerIdentity.matches(identity.publicKey, identity.deviceId))
        assertFalse(PeerIdentity.matches(identity.publicKey, DeviceId("abcdefghij234567")))
    }

    @Test
    fun `survives a storage round trip`() {
        val original = IdentityKeyPair.generate()
        val restored = IdentityKeyPair.fromPrivateKey(original.exportPrivateKeyForStorage())
        assertEquals(original.deviceId, restored.deviceId)
        assertArrayEquals(original.publicKey, restored.publicKey)
    }

    @Test
    fun `generates a fresh key pair every time`() {
        assertNotEquals(IdentityKeyPair.generate().deviceId, IdentityKeyPair.generate().deviceId)
        assertEquals(ProtocolConstants.DEVICE_ID_LENGTH, IdentityKeyPair.generate().deviceId.value.length)
    }

    @Test
    fun `hands out copies of the public key`() {
        val identity = IdentityKeyPair.generate()
        identity.publicKey.fill(0)
        assertFalse(CryptoPrimitives.isAllZero(identity.publicKey))
        assertEquals(HandshakeConstants.X25519_KEY_BYTES, identity.publicKey.size)
    }

    @Test
    fun `generates a fresh ephemeral key pair every time`() {
        val first = EphemeralKeyPair.generate()
        val second = EphemeralKeyPair.generate()
        assertEquals(HandshakeConstants.X25519_KEY_BYTES, first.publicKey.size)
        assertNotEquals(Hex.encodeLower(first.publicKey), Hex.encodeLower(second.publicKey))
    }

    private companion object {
        const val ALICE_PRIVATE = "77076d0a7318a57d3c16c17251b26645df4c2f87ebc0992ab177fba51db92c2a"
        const val ALICE_PUBLIC = "8520f0098930a754748b7ddcb43ef75a0dbf3a0d26381af4eba4a98eaa9b4e6a"
    }
}
