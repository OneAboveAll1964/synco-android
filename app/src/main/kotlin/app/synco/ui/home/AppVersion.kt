package app.synco.ui.home

import android.content.Context

object AppVersion {

    fun of(context: Context): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull().orEmpty().ifBlank { FALLBACK }

    private const val FALLBACK = "development"
}
