package app.synco.clipboard

import app.synco.protocol.clip.ClipHash
import app.synco.protocol.message.ClipRep

internal object EchoHashes {
    fun of(applied: List<ClipRep>, plainText: String?): Set<String> {
        val hashes = mutableSetOf(ClipHash.compute(RepOrder.sortedReps(applied)))
        val text = plainText?.takeIf { it.isNotEmpty() }?.let { ClipRep.Text(it) } ?: return hashes
        hashes += ClipHash.compute(listOf(text))
        applied.filterIsInstance<ClipRep.Html>().firstOrNull()?.let {
            hashes += ClipHash.compute(listOf(it, text))
        }
        applied.filterIsInstance<ClipRep.Url>().firstOrNull()?.let {
            hashes += ClipHash.compute(listOf(it, text))
        }
        if (WebUrls.isWebUrl(text.text)) {
            hashes += ClipHash.compute(listOf(ClipRep.Url(text.text.trim()), text))
        }
        return hashes
    }
}
