package app.synco.transfer

import java.io.File

interface ReceivedFileDestination {

    val isChosen: Boolean

    suspend fun publish(source: File, name: String, relativePath: String?): PublishedFile?
}
