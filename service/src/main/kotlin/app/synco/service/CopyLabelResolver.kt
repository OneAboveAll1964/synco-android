package app.synco.service

import android.content.Context
import app.synco.clipboard.CopyLabels

internal object CopyLabelResolver {

    fun resolve(context: Context): CopyLabels {
        val resolved = runCatching { context.getString(android.R.string.copy) }.getOrNull()
        return CopyLabels.withFallback(resolved)
    }
}
