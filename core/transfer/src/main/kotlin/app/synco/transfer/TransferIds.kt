package app.synco.transfer

import java.util.UUID

object TransferIds {
    fun newId(): UUID = UUID.randomUUID()

    fun parseOrNull(raw: String): UUID? = runCatching { UUID.fromString(raw) }.getOrNull()
}
