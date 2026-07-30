package app.synco.protocol.message

enum class AckReason(val wireValue: String) {
    TYPE_DISABLED("typeDisabled"),
    RECEIVE_DISABLED("receiveDisabled"),
    TOO_LARGE("tooLarge"),
    HASH_MISMATCH("hashMismatch"),
    USER_CANCELLED("userCancelled"),
    ;

    companion object {
        fun fromWire(wireValue: String?): AckReason? =
            wireValue?.let { value -> entries.firstOrNull { it.wireValue == value } }
    }
}
