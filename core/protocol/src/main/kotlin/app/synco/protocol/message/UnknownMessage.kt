package app.synco.protocol.message

import kotlinx.serialization.json.JsonObject

data class UnknownMessage(val type: String, val body: JsonObject) : Envelope
