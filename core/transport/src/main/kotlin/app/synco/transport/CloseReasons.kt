package app.synco.transport

import app.synco.protocol.SyncoError
import app.synco.protocol.message.CloseReason

internal object CloseReasons {

    fun of(error: Throwable): CloseReason = when (error) {
        is SyncoError -> error.closeReason ?: CloseReason.SHUTDOWN
        is TransportError -> error.closeReason ?: CloseReason.SHUTDOWN
        else -> CloseReason.SHUTDOWN
    }
}
