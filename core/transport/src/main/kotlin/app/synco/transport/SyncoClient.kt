package app.synco.transport

import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException

class SyncoClient(
    private val sockets: SyncoSocketFactory,
    private val connectTimeoutMillis: Long = TransportConstants.CONNECT_TIMEOUT_MILLIS,
) {
    suspend fun connect(host: String, port: Int): TransportConnection {
        val socket = withTimeoutOrNull(connectTimeoutMillis) {
            try {
                sockets.connect(host, port)
            } catch (failure: IOException) {
                throw TransportError.ConnectFailed(host, port, failure)
            }
        } ?: throw TransportError.ConnectTimeout(host, port, connectTimeoutMillis)
        return TransportConnection(socket)
    }
}
