package app.synco.transfer

import java.io.File
import java.util.UUID

interface TransferStorage {
    fun stagingFile(transferId: UUID): File

    fun createReceivedFile(name: String, relativePath: String? = null): File

    fun clearStaging()
}
