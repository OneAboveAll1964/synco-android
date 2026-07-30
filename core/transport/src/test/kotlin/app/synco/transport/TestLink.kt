package app.synco.transport

import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.ByteReadChannel

internal class TestLink(elapsedMillis: () -> Long = ::monotonicMillis) {

    private val leftToRight = ByteChannel(autoFlush = false)
    private val rightToLeft = ByteChannel(autoFlush = false)

    val wireFromLeft: ByteReadChannel get() = leftToRight

    val left = FramedConnection(
        input = rightToLeft,
        output = leftToRight,
        activity = ConnectionActivity(elapsedMillis),
    )

    val right = FramedConnection(
        input = leftToRight,
        output = rightToLeft,
        activity = ConnectionActivity(elapsedMillis),
    )
}
