package app.synco.clipboard

import app.synco.protocol.message.ClipRep
import app.synco.protocol.message.ClipRepKind

internal object RepOrder {
    private val RANK = listOf(
        ClipRepKind.IMAGE,
        ClipRepKind.FILE,
        ClipRepKind.HTML,
        ClipRepKind.RTF,
        ClipRepKind.URL,
        ClipRepKind.TEXT,
    )

    fun rank(kind: String): Int = RANK.indexOf(kind).takeIf { it >= 0 } ?: RANK.size

    fun sorted(prepared: List<PreparedRep>): List<PreparedRep> = prepared.sortedBy { rank(it.rep.kind) }

    fun sortedReps(reps: List<ClipRep>): List<ClipRep> = reps.sortedBy { rank(it.kind) }
}
