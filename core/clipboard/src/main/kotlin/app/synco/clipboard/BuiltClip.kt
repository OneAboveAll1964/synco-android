package app.synco.clipboard

import android.content.ClipData
import app.synco.protocol.message.ClipRep

class BuiltClip(
    val data: ClipData,
    val appliedReps: List<ClipRep>,
    val echoHashes: Set<String>,
)
