package app.synco.transfer

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object TransferFileUris {
    const val AUTHORITY_SUFFIX = ".files"

    fun authority(context: Context): String = context.packageName + AUTHORITY_SUFFIX

    fun contentUriFor(context: Context, file: File): Uri? =
        runCatching { FileProvider.getUriForFile(context, authority(context), file) }.getOrNull()
}
