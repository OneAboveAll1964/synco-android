package app.synco.protocol.message

import app.synco.protocol.SyncoError
import app.synco.protocol.SyncoJson
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

object EnvelopeCodec {

    fun encodeToString(envelope: Envelope): String = when (envelope) {
        is ControlMessage -> SyncoJson.encodeToString(ControlMessage.serializer(), envelope)
        is UnknownMessage -> envelope.body.toString()
    }

    fun encodeToBytes(envelope: Envelope): ByteArray = encodeToString(envelope).encodeToByteArray()

    fun decode(bytes: ByteArray): Envelope = decode(bytes.decodeToString())

    fun decode(text: String): Envelope {
        val body = parseObject(text)
        val type = discriminatorOf(body)
        if (type !in MessageType.KNOWN) return UnknownMessage(type, body)
        return try {
            SyncoJson.decodeFromJsonElement(ControlMessage.serializer(), body)
        } catch (error: IllegalArgumentException) {
            throw SyncoError.Malformed("malformed $type message: ${error.message}")
        }
    }

    private fun parseObject(text: String): JsonObject {
        val element = try {
            SyncoJson.parseToJsonElement(text)
        } catch (error: IllegalArgumentException) {
            throw SyncoError.Malformed("control frame is not JSON: ${error.message}")
        }
        return element as? JsonObject
            ?: throw SyncoError.Malformed("control frame is not a JSON object")
    }

    private fun discriminatorOf(body: JsonObject): String {
        val discriminator = body[MessageType.DISCRIMINATOR] as? JsonPrimitive
        return discriminator?.takeIf { it.isString }?.content
            ?: throw SyncoError.Malformed("control frame has no '${MessageType.DISCRIMINATOR}' field")
    }
}
