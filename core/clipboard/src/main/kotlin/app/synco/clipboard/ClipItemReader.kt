package app.synco.clipboard

import android.content.ClipData
import android.net.Uri
import app.synco.protocol.ProtocolConstants
import app.synco.protocol.message.ClipRep
import app.synco.transfer.ContentUriMetadata

internal class ClipItemReader(
    private val blobs: BlobRepFactory,
    private val metadata: ContentUriMetadata,
) {
    suspend fun reps(clipId: String, item: ClipData.Item, maxBlobBytes: Long): List<PreparedRep> {
        val prepared = mutableListOf<PreparedRep>()
        val uri = item.uri
        val text = item.text?.toString()?.takeIf { it.isNotEmpty() }
        if (uri != null) {
            if (metadata.isStreamable(uri)) {
                blobs.fromUri(clipId, uri, maxBlobBytes)?.let { prepared += it }
            } else if (item.intent == null) {
                prepared += PreparedRep(ClipRep.Url(uri.toString(), titleOf(text, uri)))
            }
        }
        item.htmlText?.takeIf { it.isNotEmpty() }?.let { html ->
            val rep = inlineOrFile(clipId, html, HTML_NAME, HTML_MIME, maxBlobBytes) { ClipRep.Html(it) }
            rep?.let { prepared += it }
        }
        if (text != null) {
            if (uri == null && WebUrls.isWebUrl(text)) prepared += PreparedRep(ClipRep.Url(text.trim()))
            val rep = inlineOrFile(clipId, text, TEXT_NAME, TEXT_MIME, maxBlobBytes) { ClipRep.Text(it) }
            rep?.let { prepared += it }
        }
        return prepared
    }

    private suspend fun inlineOrFile(
        clipId: String,
        value: String,
        name: String,
        mime: String,
        maxBlobBytes: Long,
        rep: (String) -> ClipRep,
    ): PreparedRep? {
        val encoded = value.toByteArray(Charsets.UTF_8)
        if (encoded.size < ProtocolConstants.INLINE_REP_MAX_BYTES) return PreparedRep(rep(value))
        return blobs.fromBytes(clipId, name, mime, encoded, maxBlobBytes)
    }

    private fun titleOf(text: String?, uri: Uri): String? =
        text?.takeIf { it.isNotBlank() && it != uri.toString() }

    private companion object {
        const val TEXT_NAME = "clipboard.txt"
        const val TEXT_MIME = "text/plain"
        const val HTML_NAME = "clipboard.html"
        const val HTML_MIME = "text/html"
    }
}
