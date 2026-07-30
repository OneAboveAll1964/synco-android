package app.synco.transport

internal class ConnectionActivity(private val elapsedMillis: () -> Long = ::monotonicMillis) {

    @Volatile
    private var lastReadAtMillis: Long = elapsedMillis()

    @Volatile
    private var lastWriteAtMillis: Long = elapsedMillis()

    val millisSinceRead: Long get() = elapsedMillis() - lastReadAtMillis

    val millisSinceWrite: Long get() = elapsedMillis() - lastWriteAtMillis

    fun recordRead() {
        lastReadAtMillis = elapsedMillis()
    }

    fun recordWrite() {
        lastWriteAtMillis = elapsedMillis()
    }
}

internal fun monotonicMillis(): Long = System.nanoTime() / NANOS_PER_MILLI

private const val NANOS_PER_MILLI = 1_000_000L
