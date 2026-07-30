package app.synco.crypto

import app.synco.protocol.HandshakeConstants
import app.synco.protocol.SyncoError
import app.synco.protocol.encoding.Hex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CryptoPrimitivesTest {

    @Test
    fun `matches rfc 5869 hkdf sha256 test case one`() {
        val okm = CryptoPrimitives.hkdfSha256(
            ikm = ByteArray(22) { 0x0B },
            salt = ByteArray(13) { it.toByte() },
            info = ByteArray(10) { (0xF0 + it).toByte() },
            length = 42,
        )
        assertEquals(
            "3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf" +
                "34007208d5b887185865",
            Hex.encodeLower(okm),
        )
    }

    @Test
    fun `hashes concatenated inputs as one message`() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            Hex.encodeLower(CryptoPrimitives.sha256("abc".toByteArray())),
        )
        assertEquals(
            Hex.encodeLower(CryptoPrimitives.sha256("abc".toByteArray())),
            Hex.encodeLower(CryptoPrimitives.sha256("a".toByteArray(), "bc".toByteArray())),
        )
    }

    @Test
    fun `matches rfc 4231 hmac sha256 test case one`() {
        assertEquals(
            "b0344c61d8db38535ca8afceaf0bf12b881dc200c9833da726e9376c2e32cff7",
            Hex.encodeLower(CryptoPrimitives.hmacSha256(ByteArray(20) { 0x0B }, "Hi There".toByteArray())),
        )
    }

    @Test
    fun `matches rfc 7748 x25519 test vectors in both directions`() {
        assertEquals(SHARED, Hex.encodeLower(CryptoPrimitives.x25519(Hex.decode(ALICE_PRIVATE), Hex.decode(BOB_PUBLIC))))
        assertEquals(SHARED, Hex.encodeLower(CryptoPrimitives.x25519(Hex.decode(BOB_PRIVATE), Hex.decode(ALICE_PUBLIC))))
    }

    @Test
    fun `rejects an agreement that yields an all zero secret`() {
        assertThrows(SyncoError.BadHandshake::class.java) {
            CryptoPrimitives.x25519(Hex.decode(ALICE_PRIVATE), ByteArray(HandshakeConstants.X25519_KEY_BYTES))
        }
    }

    @Test
    fun `rejects keys of the wrong length`() {
        assertThrows(IllegalArgumentException::class.java) {
            CryptoPrimitives.x25519(ByteArray(31), ByteArray(HandshakeConstants.X25519_KEY_BYTES))
        }
    }

    @Test
    fun `compares byte arrays in constant time`() {
        val left = ByteArray(32) { it.toByte() }
        assertTrue(CryptoPrimitives.constantTimeEquals(left, left.copyOf()))
        assertFalse(CryptoPrimitives.constantTimeEquals(left, left.copyOf().also { it[31] = 0 }))
        assertFalse(CryptoPrimitives.constantTimeEquals(left, left.copyOf(31)))
    }

    @Test
    fun `produces distinct random bytes of the requested length`() {
        val first = CryptoPrimitives.randomBytes(32)
        assertEquals(32, first.size)
        assertNotEquals(Hex.encodeLower(first), Hex.encodeLower(CryptoPrimitives.randomBytes(32)))
        assertFalse(CryptoPrimitives.isAllZero(first))
    }

    private companion object {
        const val ALICE_PRIVATE = "77076d0a7318a57d3c16c17251b26645df4c2f87ebc0992ab177fba51db92c2a"
        const val ALICE_PUBLIC = "8520f0098930a754748b7ddcb43ef75a0dbf3a0d26381af4eba4a98eaa9b4e6a"
        const val BOB_PRIVATE = "5dab087e624a8a4b79e17f8b83800ee66f3bb1292618b6fd1c2f8b27ff88e0eb"
        const val BOB_PUBLIC = "de9edb7d7b7dc1b4d35b61c2ece435373f8343c85b78674dadfc7e146f882b4f"
        const val SHARED = "4a5d9d5ba4ce2de1728e3bf480350f25e07e21c947d19e3376f09b3c1e161742"
    }
}
