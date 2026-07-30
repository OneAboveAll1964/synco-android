package app.synco.transport

import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.network.sockets.port

class TransportConnection internal constructor(private val socket: Socket) {

    val remoteHost: String = (socket.remoteAddress as? InetSocketAddress)?.hostname ?: UNKNOWN_HOST

    val remotePort: Int = socket.remoteAddress.port()

    internal val frames: FramedConnection = FramedConnection(
        input = socket.openReadChannel(),
        output = socket.openWriteChannel(),
        onClose = socket::close,
    )

    suspend fun close() {
        frames.close()
    }

    override fun toString(): String = "TransportConnection($remoteHost:$remotePort)"

    private companion object {
        const val UNKNOWN_HOST = "unknown"
    }
}
