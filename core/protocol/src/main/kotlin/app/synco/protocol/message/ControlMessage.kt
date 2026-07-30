package app.synco.protocol.message

import kotlinx.serialization.Serializable

@Serializable
sealed interface ControlMessage : Envelope
