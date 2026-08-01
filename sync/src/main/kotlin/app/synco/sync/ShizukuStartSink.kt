package app.synco.sync

fun interface ShizukuStartSink {
    fun report(report: ShizukuStartReport)

    companion object {
        val NONE = ShizukuStartSink { }
    }
}

interface ShizukuStartReports {
    fun clear()
}
