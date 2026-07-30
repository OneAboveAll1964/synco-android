package app.synco.transfer

import android.content.Context
import java.io.File
import java.util.UUID

class TransferPaths(private val context: Context) : TransferStorage {

    val stagingDirectory: File get() = created(File(root(), STAGING_DIRECTORY))

    val receivedDirectory: File get() = created(File(root(), RECEIVED_DIRECTORY))

    override fun stagingFile(transferId: UUID): File =
        File(stagingDirectory, "$transferId$STAGING_SUFFIX")

    override fun createReceivedFile(name: String, relativePath: String?): File {
        val parent = relativePath
            ?.let { created(SafeFileName.resolveDirectory(receivedDirectory, it)) }
            ?: receivedDirectory
        return SafeFileName.unique(parent, SafeFileName.of(name))
    }

    override fun clearStaging() {
        stagingDirectory.listFiles()?.forEach { it.delete() }
    }

    private fun root(): File = File(context.getExternalFilesDir(null) ?: context.filesDir, ROOT_DIRECTORY)

    private fun created(directory: File): File = directory.also { if (!it.isDirectory) it.mkdirs() }

    companion object {
        const val ROOT_DIRECTORY = "synco"
        const val STAGING_DIRECTORY = "staging"
        const val RECEIVED_DIRECTORY = "received"
        const val STAGING_SUFFIX = ".part"
        const val FILE_PATHS_RESOURCE_NAME = "synco_file_paths"
    }
}
