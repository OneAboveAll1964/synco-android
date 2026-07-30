package app.synco.transport

import app.synco.protocol.message.CloseReason

sealed class TransportError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    abstract val closeReason: CloseReason?

    class BindFailed(val host: String, cause: Throwable) :
        TransportError("could not bind a listener on $host", cause) {
        override val closeReason: CloseReason? = null
    }

    class ConnectFailed(val host: String, val port: Int, cause: Throwable) :
        TransportError("could not connect to $host:$port", cause) {
        override val closeReason: CloseReason? = null
    }

    class ConnectTimeout(val host: String, val port: Int, val timeoutMillis: Long) :
        TransportError("connecting to $host:$port gave up after ${timeoutMillis}ms") {
        override val closeReason = CloseReason.TIMEOUT
    }

    class PeerClosed(cause: Throwable? = null) :
        TransportError("the peer closed the connection", cause) {
        override val closeReason = CloseReason.SHUTDOWN
    }

    class LinkFailed(detail: String, cause: Throwable) : TransportError(detail, cause) {
        override val closeReason: CloseReason? = null
    }

    class PeerGoodbye(val reason: CloseReason?) :
        TransportError("the peer said bye: ${reason?.wireValue ?: "unspecified"}") {
        override val closeReason: CloseReason? = reason
    }

    class NotEstablished : TransportError("the session ended before it was established") {
        override val closeReason: CloseReason? = null
    }
}
