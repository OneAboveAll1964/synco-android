package app.synco.sync

import app.synco.protocol.message.Clip
import app.synco.protocol.message.ClipRep
import java.io.File
import java.util.UUID

internal class InboundClipAssembly(val clip: Clip, val reps: List<ClipRep>) {

    private val relativePaths: Map<UUID, String> = reps
        .mapNotNull { rep ->
            val transferId = StreamedReps.uuidOf(rep) ?: return@mapNotNull null
            StreamedReps.relativePathOf(rep)?.let { transferId to it }
        }
        .toMap()

    private val outstanding: MutableSet<UUID> = reps.mapNotNull(StreamedReps::uuidOf).toMutableSet()

    private val received = mutableMapOf<String, File>()

    val transferIds: Set<UUID> = reps.mapNotNull(StreamedReps::uuidOf).toSet()

    val remaining: Set<UUID> get() = outstanding.toSet()

    val awaitsTransfers: Boolean get() = outstanding.isNotEmpty()

    val blobs: Map<String, File> get() = received.toMap()

    fun relativePathOf(transferId: UUID): String? = relativePaths[transferId]

    fun record(transferId: UUID, file: File) {
        outstanding.remove(transferId)
        received[transferId.toString()] = file
    }
}
