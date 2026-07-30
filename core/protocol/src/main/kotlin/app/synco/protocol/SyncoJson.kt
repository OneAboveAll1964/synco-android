package app.synco.protocol

import app.synco.protocol.message.MessageType
import kotlinx.serialization.json.Json

val SyncoJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
    classDiscriminator = MessageType.DISCRIMINATOR
}
