package app.synco.sync

import app.synco.crypto.PeerIdentity
import app.synco.protocol.DeviceId
import app.synco.protocol.HandshakeConstants
import app.synco.protocol.encoding.Base64Codec
import java.net.URLDecoder
import java.util.Base64

data class QRPairPayload(
    val deviceId: DeviceId,
    val staticPublicKey: String,
    val displayName: String,
    val hosts: List<String>,
    val port: Int,
    val token: String,
) {
    companion object {
        fun parse(text: String): QRPairPayload? {
            val query = text.trim().takeIf { it.startsWith(SCHEME) }?.substringAfter('?', "") ?: return null
            val fields = query.split('&').mapNotNull { pair ->
                val key = pair.substringBefore('=', "")
                if (key.isEmpty() || !pair.contains('=')) return@mapNotNull null
                key to URLDecoder.decode(pair.substringAfter('='), Charsets.UTF_8)
            }.toMap()
            if (fields["v"] != "1") return null
            val deviceId = fields["did"]?.let(DeviceId::parseOrNull) ?: return null
            val key = fields["key"]?.let(::decodeUrlSafe) ?: return null
            if (key.size != HandshakeConstants.X25519_KEY_BYTES) return null
            if (!PeerIdentity.matches(key, deviceId)) return null
            if (fields["fp"] != PeerIdentity.fingerprintOf(key).grouped) return null
            val port = fields["port"]?.toIntOrNull()?.takeIf { it in 1..65_535 } ?: return null
            val hosts = fields["hosts"].orEmpty().split(',').map(String::trim).filter(String::isNotEmpty)
            if (hosts.isEmpty()) return null
            val token = fields["tok"]?.takeIf(String::isNotBlank) ?: return null
            return QRPairPayload(
                deviceId = deviceId,
                staticPublicKey = Base64Codec.encode(key),
                displayName = fields["name"]?.takeIf(String::isNotBlank) ?: deviceId.value,
                hosts = hosts,
                port = port,
                token = token,
            )
        }

        private fun decodeUrlSafe(encoded: String): ByteArray? = runCatching {
            val padded = encoded + "=".repeat((4 - encoded.length % 4) % 4)
            Base64.getUrlDecoder().decode(padded)
        }.getOrNull()

        private const val SCHEME = "synco://pair"
    }
}
