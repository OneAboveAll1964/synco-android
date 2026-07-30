package app.synco.transport

import app.synco.protocol.DeviceId
import app.synco.protocol.message.CloseReason

sealed interface SessionOutcome {
    val closeReason: CloseReason

    data class Ended(
        val peerDeviceId: DeviceId?,
        override val closeReason: CloseReason,
        val cause: Throwable? = null,
    ) : SessionOutcome

    data class Pairing(val result: PairingResult) : SessionOutcome {
        override val closeReason: CloseReason get() = CloseReason.UNPAIRED
    }
}
