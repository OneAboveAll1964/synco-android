package app.synco.transfer

import app.synco.protocol.message.AckReason
import app.synco.protocol.message.TransferAbort
import java.util.UUID

enum class TransferFailure(val wireValue: String, val ackReason: AckReason?) {
    TOO_LARGE("tooLarge", AckReason.TOO_LARGE),
    HASH_MISMATCH("hashMismatch", AckReason.HASH_MISMATCH),
    CANCELLED("userCancelled", AckReason.USER_CANCELLED),
    OFFSET_MISMATCH("offsetMismatch", null),
    INCOMPLETE("incomplete", null),
    WRITE_FAILED("writeFailed", null),
    UNKNOWN_TRANSFER("unknownTransfer", null),
    CLOSED("closed", null),
    SHUTDOWN("shutdown", null),
    ;

    fun toAbort(transferId: UUID): TransferAbort = TransferAbort(transferId.toString(), wireValue)

    companion object {
        fun fromWire(wireValue: String?): TransferFailure? =
            wireValue?.let { value -> entries.firstOrNull { it.wireValue == value } }
    }
}
