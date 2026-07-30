package app.synco.ui.home

import android.net.Uri
import android.provider.DocumentsContract

object DownloadsTree {

    fun initialUri(): Uri? = runCatching {
        DocumentsContract.buildDocumentUri(EXTERNAL_STORAGE_PROVIDER, DOWNLOAD_DOCUMENT_ID)
    }.getOrNull()

    private const val EXTERNAL_STORAGE_PROVIDER = "com.android.externalstorage.documents"
    private const val DOWNLOAD_DOCUMENT_ID = "primary:Download"
}
