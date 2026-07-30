package app.synco.clipboard

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.net.Uri
import androidx.core.text.HtmlCompat
import app.synco.protocol.message.ClipRep
import app.synco.transfer.ContentMetadata
import app.synco.transfer.TransferFileUris
import java.io.File

class ClipDataBuilder(private val context: Context) {

    fun build(reps: List<ClipRep>, blobs: Map<String, File>): BuiltClip? {
        val text = reps.filterIsInstance<ClipRep.Text>().firstOrNull()?.text
        val html = reps.filterIsInstance<ClipRep.Html>().firstOrNull()?.html
        val url = reps.filterIsInstance<ClipRep.Url>().firstOrNull()?.url
        val files = localFiles(reps, blobs)
        val uris = files.mapNotNull { (id, file) -> TransferFileUris.contentUriFor(context, file)?.let { id to it } }
            .toMap()
        val plain = text ?: url ?: html?.let { plainTextOf(it) }
        val primaryUri = uris.values.firstOrNull() ?: url?.let { parseOrNull(it) }
        if (plain == null && primaryUri == null) return null
        val description = ClipDescription(LABEL, mimeTypes(plain, html, uris.values, url != null).toTypedArray())
        val data = ClipData(description, ClipData.Item(plain, html, null, primaryUri))
        uris.values.drop(1).forEach { extra ->
            runCatching { data.addItem(context.contentResolver, ClipData.Item(extra)) }
        }
        uris.values.forEach { UriReadGrants.grant(context, it) }
        val applied = appliedReps(reps, files.mapValues { it.value.name })
        return BuiltClip(data, applied, EchoHashes.of(applied, plain))
    }

    private fun localFiles(reps: List<ClipRep>, blobs: Map<String, File>): Map<String, File> =
        reps.mapNotNull { rep -> blobIdOf(rep)?.let { id -> blobs[id]?.let { id to it } } }.toMap()

    private fun appliedReps(reps: List<ClipRep>, localNames: Map<String, String>): List<ClipRep> = reps.map { rep ->
        when (rep) {
            is ClipRep.Image -> localNames[rep.transferId]?.let { rep.copy(name = it) } ?: rep
            is ClipRep.File -> localNames[rep.transferId]?.let { rep.copy(name = it) } ?: rep
            else -> rep
        }
    }

    private fun mimeTypes(
        plain: String?,
        html: String?,
        uris: Collection<Uri>,
        hasUrl: Boolean,
    ): List<String> {
        val types = mutableListOf<String>()
        uris.forEach { types += context.contentResolver.getType(it) ?: ContentMetadata.DEFAULT_MIME }
        if (html != null) types += ClipDescription.MIMETYPE_TEXT_HTML
        if (plain != null) types += ClipDescription.MIMETYPE_TEXT_PLAIN
        if (uris.isNotEmpty() || hasUrl) types += ClipDescription.MIMETYPE_TEXT_URILIST
        return types.distinct()
    }

    private fun blobIdOf(rep: ClipRep): String? = when (rep) {
        is ClipRep.Image -> rep.transferId
        is ClipRep.File -> rep.transferId
        else -> null
    }

    private fun plainTextOf(html: String): String =
        HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()

    private fun parseOrNull(value: String): Uri? = runCatching { Uri.parse(value) }.getOrNull()

    private companion object {
        const val LABEL = "Synco"
    }
}
