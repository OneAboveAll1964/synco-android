package app.synco.sync

import app.synco.protocol.message.ClipRep
import app.synco.protocol.message.ClipRepKind
import app.synco.transfer.TransferIds
import java.util.UUID

internal object StreamedReps {

    fun transferIdOf(rep: ClipRep): String? = when (rep) {
        is ClipRep.Image -> rep.transferId
        is ClipRep.File -> rep.transferId
        else -> null
    }

    fun uuidOf(rep: ClipRep): UUID? = transferIdOf(rep)?.let(TransferIds::parseOrNull)

    fun relativePathOf(rep: ClipRep): String? = (rep as? ClipRep.File)?.rel

    fun nameOf(rep: ClipRep): String? = when (rep) {
        is ClipRep.Image -> rep.name
        is ClipRep.File -> rep.name
        else -> null
    }

    fun isReconstructable(rep: ClipRep): Boolean =
        rep.kind in ClipRepKind.INLINE || uuidOf(rep) != null
}
