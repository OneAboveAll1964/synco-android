package app.synco.sync

import android.content.Context
import app.synco.clipboard.BlobRepFactory
import app.synco.transfer.ContentUriMetadata
import app.synco.transfer.TransferManager
import app.synco.transfer.TransferPaths
import app.synco.transfer.TransferStorage

internal class TransferLayer(context: Context) {

    private val resolver = context.contentResolver

    val storage: TransferStorage = TransferPaths(context)

    val metadata = ContentUriMetadata(resolver)

    val manager = TransferManager(storage, resolver)

    val gateway: TransferGateway = ManagedTransferGateway(manager)

    val blobs = BlobRepFactory(metadata, storage, manager)

    val progress = manager.progress
}
