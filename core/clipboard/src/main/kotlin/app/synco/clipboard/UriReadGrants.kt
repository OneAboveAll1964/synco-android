package app.synco.clipboard

import android.content.Context
import android.content.Intent
import android.net.Uri

internal object UriReadGrants {
    private val TARGET_PACKAGES = listOf("android", "com.android.systemui")

    fun grant(context: Context, uri: Uri) {
        TARGET_PACKAGES.forEach { target ->
            runCatching {
                context.grantUriPermission(target, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }
}
