package app.synco.sync

import app.synco.logging.SyncoLog
import app.synco.transfer.ReceivedFileDestination

internal class InboundFilePublisher(
    private val destination: ReceivedFileDestination,
    private val announcer: ReceivedFileAnnouncer,
) {
    suspend fun publish(assembly: InboundClipAssembly) {
        val blobs = assembly.blobs
        if (blobs.isEmpty()) return
        val names = mutableListOf<String>()
        var location: String? = null
        for (rep in assembly.reps) {
            val transferId = StreamedReps.transferIdOf(rep) ?: continue
            val file = blobs[transferId] ?: continue
            val name = StreamedReps.nameOf(rep) ?: file.name
            val published = destination.publish(file, name, StreamedReps.relativePathOf(rep))
            if (published == null) {
                names += name
                continue
            }
            names += published.name
            location = published.location.substringBeforeLast('/', published.location)
        }
        if (names.isEmpty()) return
        SyncoLog.transfer.info("received ${names.size} file(s)")
        announcer.announce(
            ReceivedFileArrival(
                names = names,
                location = location,
                browsable = destination.isChosen && location != null,
            ),
        )
    }
}
