package app.synco.sync

import android.content.ClipboardManager
import android.content.Context
import app.synco.clipboard.ClipDataBuilder
import app.synco.clipboard.ClipboardCapture
import app.synco.clipboard.ClipboardCaptureHub
import app.synco.clipboard.ClipboardReader
import app.synco.clipboard.ManualClips
import app.synco.clipboard.ClipboardWriter
import app.synco.clipboard.ClipboardGeneration
import app.synco.clipboard.StagedBlobs

internal class ClipboardLayer(
    context: Context,
    transfers: TransferLayer,
    maxBlobBytes: () -> Long,
    captureWaitMillis: () -> Long,
) {

    private val clipboardManager =
        requireNotNull(context.getSystemService(ClipboardManager::class.java)) {
            "this device has no ClipboardManager"
        }

    private val generation = ClipboardGeneration()

    private val reader = ClipboardReader(clipboardManager, transfers.blobs, transfers.metadata)

    private val hub = ClipboardCaptureHub(
        reader = reader,
        generation = generation,
        maxBlobBytes = maxBlobBytes,
        captureWaitMillis = captureWaitMillis,
        staged = StagedBlobs { snapshot ->
            snapshot.transfers.forEach { transfers.manager.abortOutgoing(it.transferId) }
        },
    )

    val capture: ClipboardCapture = hub

    val manual = ManualClips(transfers.blobs, transfers.metadata)

    val sink: ClipboardSink = ClipboardWriterSink(
        ClipboardWriter(clipboardManager, ClipDataBuilder(context), generation),
    )
}
