package app.synco.transport

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class SyncoSocketFactory(dispatcher: CoroutineDispatcher = Dispatchers.IO) : AutoCloseable {

    private val selector: SelectorManager = SelectorManager(dispatcher)

    internal suspend fun bind(host: String, port: Int): ServerSocket =
        aSocket(selector).tcp().bind(hostname = host, port = port)

    internal suspend fun connect(host: String, port: Int): Socket =
        aSocket(selector).tcp().connect(hostname = host, port = port)

    override fun close() {
        selector.close()
    }
}
