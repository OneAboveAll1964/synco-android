package app.synco.protocol.encoding

import java.util.Base64

object Base64Codec {
    private val encoder: Base64.Encoder = Base64.getEncoder()
    private val decoder: Base64.Decoder = Base64.getDecoder()

    fun encode(bytes: ByteArray): String = encoder.encodeToString(bytes)

    fun decode(text: String): ByteArray = decoder.decode(text)

    fun decodeOrNull(text: String): ByteArray? = runCatching { decoder.decode(text) }.getOrNull()
}
