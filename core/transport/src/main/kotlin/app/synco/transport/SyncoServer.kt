package app.synco.transport

import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.port
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class SyncoServer private constructor(private val socket: ServerSocket) : AutoCloseable {

    val port: Int = socket.port

    val connections: Flow<TransportConnection> = flow {
        while (true) {
            val accepted = try {
                socket.accept()
            } catch (closed: IOException) {
                break
            }
            emit(TransportConnection(accepted))
        }
    }

    override fun close() {
        socket.close()
    }

    companion object {
        suspend fun bind(
            sockets: SyncoSocketFactory,
            host: String = TransportConstants.BIND_ADDRESS,
        ): SyncoServer = try {
            SyncoServer(sockets.bind(host, TransportConstants.EPHEMERAL_PORT))
        } catch (failure: IOException) {
            throw TransportError.BindFailed(host, failure)
        }
    }
}
