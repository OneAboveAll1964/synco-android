package app.synco.sync

import app.synco.crypto.IdentityKeyPair
import app.synco.crypto.PeerIdentity
import java.net.URLEncoder
import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QRPairPayloadTest {

    private val key = IdentityKeyPair.generate().publicKey
    private val deviceId = PeerIdentity.deviceIdOf(key)
    private val fingerprint = PeerIdentity.fingerprintOf(key)

    private fun payload(
        did: String = deviceId.value,
        fp: String = fingerprint.grouped,
        port: String = "49152",
        hosts: String = "192.168.1.20,10.0.0.4",
        tok: String = "one-time",
        version: String = "1",
    ): String {
        val encodedKey = Base64.getUrlEncoder().withoutPadding().encodeToString(key)
        val name = URLEncoder.encode("Shko's MacBook Pro", Charsets.UTF_8)
        return "synco://pair?v=$version&did=$did&key=$encodedKey&fp=$fp&name=$name" +
            "&port=$port&hosts=$hosts&tok=$tok"
    }

    @Test
    fun `a genuine code parses with every field`() {
        val parsed = QRPairPayload.parse(payload())

        assertEquals(deviceId, parsed?.deviceId)
        assertEquals("Shko's MacBook Pro", parsed?.displayName)
        assertEquals(listOf("192.168.1.20", "10.0.0.4"), parsed?.hosts)
        assertEquals(49152, parsed?.port)
        assertEquals("one-time", parsed?.token)
    }

    @Test
    fun `a code whose key does not hash to the claimed device is refused`() {
        val other = PeerIdentity.deviceIdOf(IdentityKeyPair.generate().publicKey)

        assertNull(QRPairPayload.parse(payload(did = other.value)))
    }

    @Test
    fun `a code whose fingerprint lies is refused`() {
        val other = PeerIdentity.fingerprintOf(IdentityKeyPair.generate().publicKey)

        assertNull(QRPairPayload.parse(payload(fp = other.grouped)))
    }

    @Test
    fun `garbage and missing pieces are refused`() {
        assertNull(QRPairPayload.parse("https://example.com/?did=x"))
        assertNull(QRPairPayload.parse(payload(did = "nonsense")))
        assertNull(QRPairPayload.parse(payload(did = "UPPER!chars#bad$")))
        assertNull(QRPairPayload.parse(payload(port = "0")))
        assertNull(QRPairPayload.parse(payload(port = "99999")))
        assertNull(QRPairPayload.parse(payload(hosts = "")))
        assertNull(QRPairPayload.parse(payload(tok = "")))
        assertNull(QRPairPayload.parse(payload(version = "2")))
    }
}
