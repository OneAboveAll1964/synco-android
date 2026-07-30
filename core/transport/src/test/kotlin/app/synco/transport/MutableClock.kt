package app.synco.transport

internal class MutableClock {

    private var nowMillis = 0L

    val elapsedMillis: () -> Long = { nowMillis }

    fun advance(millis: Long) {
        nowMillis += millis
    }
}
