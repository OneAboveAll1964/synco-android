package app.synco.transfer

import java.io.File
import java.util.UUID

sealed interface TransferOutcome {
    val transferId: UUID

    data class Completed(
        override val transferId: UUID,
        val file: File,
        val name: String,
        val mime: String,
        val size: Long,
        val sha256: String,
    ) : TransferOutcome

    data class Failed(
        override val transferId: UUID,
        val failure: TransferFailure,
    ) : TransferOutcome
}
