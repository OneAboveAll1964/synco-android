package app.synco.protocol.message

enum class CloseReason(val wireValue: String) {
    VERSION_MISMATCH("versionMismatch"),
    SELF_CONNECTION("selfConnection"),
    UNKNOWN_KEY("unknownKey"),
    DID_MISMATCH("didMismatch"),
    BAD_HANDSHAKE("badHandshake"),
    BAD_AUTH("badAuth"),
    FRAME_TOO_LARGE("frameTooLarge"),
    REPLAY("replay"),
    TIMEOUT("timeout"),
    DUPLICATE_SESSION("duplicateSession"),
    SHUTDOWN("shutdown"),
    UNPAIRED("unpaired"),
    ;

    companion object {
        fun fromWire(wireValue: String?): CloseReason? =
            wireValue?.let { value -> entries.firstOrNull { it.wireValue == value } }
    }
}
