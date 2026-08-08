package app.synco.ui.home

import android.content.Context

object MillisText {

    fun of(millis: Long): String =
        if (millis < SECOND) "$millis ms" else "${millis / SECOND}${fraction(millis)} s"

    private fun fraction(millis: Long): String {
        val tenths = millis % SECOND / 100
        return if (tenths == 0L) "" else ".$tenths"
    }

    private const val SECOND = 1000L
}

object ByteSizeText {

    fun of(bytes: Long): String {
        if (bytes < KILOBYTE) return "$bytes B"
        if (bytes < MEGABYTE) return "${bytes / KILOBYTE} KB"
        if (bytes < GIGABYTE) return "${bytes / MEGABYTE} MB"
        val whole = bytes / GIGABYTE
        val tenths = (bytes % GIGABYTE) * 10 / GIGABYTE
        return if (tenths == 0L) "$whole GB" else "$whole.$tenths GB"
    }

    private const val KILOBYTE = 1024L
    private const val MEGABYTE = KILOBYTE * 1024
    private const val GIGABYTE = MEGABYTE * 1024
}

object AppVersion {

    fun of(context: Context): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull().orEmpty().ifBlank { FALLBACK }

    private const val FALLBACK = "development"
}
