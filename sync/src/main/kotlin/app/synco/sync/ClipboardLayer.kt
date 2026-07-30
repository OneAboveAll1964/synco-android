package app.synco.sync

import android.content.ClipboardManager
import android.content.Context
import app.synco.clipboard.ClipDataBuilder
import app.synco.clipboard.ClipboardCapture
import app.synco.clipboard.ClipboardCaptureHub
import app.synco.clipboard.ClipboardReader
import app.synco.clipboard.ClipboardWriter
import app.synco.clipboard.SuppressionWindow

internal class ClipboardLayer(context: Context, transfers: TransferLayer) {

    private val clipboardManager =
        requireNotNull(context.getSystemService(ClipboardManager::class.java)) {
            "this device has no ClipboardManager"
        }

    private val suppression = SuppressionWindow()

    private val reader = ClipboardReader(clipboardManager, transfers.blobs, transfers.metadata)

    private val hub = ClipboardCaptureHub(reader, suppression)

    val capture: ClipboardCapture = hub

    val sink: ClipboardSink = ClipboardWriterSink(
        ClipboardWriter(clipboardManager, ClipDataBuilder(context), suppression),
    )
}
