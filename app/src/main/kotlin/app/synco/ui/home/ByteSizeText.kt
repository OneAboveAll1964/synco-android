package app.synco.ui.home

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
