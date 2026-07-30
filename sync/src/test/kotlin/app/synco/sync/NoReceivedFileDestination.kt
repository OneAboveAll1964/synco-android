package app.synco.sync

import app.synco.transfer.PublishedFile
import app.synco.transfer.ReceivedFileDestination
import java.io.File

internal class NoReceivedFileDestination : ReceivedFileDestination {

    override val isChosen: Boolean = false

    override suspend fun publish(source: File, name: String, relativePath: String?): PublishedFile? = null
}
