package app.synco.protocol

import app.synco.protocol.message.CloseReason

sealed class SyncoError(message: String) : Exception(message) {
    abstract val closeReason: CloseReason?

    class VersionMismatch(val version: Int) : SyncoError("peer speaks protocol version $version") {
        override val closeReason = CloseReason.VERSION_MISMATCH
    }

    class SelfConnection(val deviceId: DeviceId) : SyncoError("peer reports our own id $deviceId") {
        override val closeReason = CloseReason.SELF_CONNECTION
    }

    class UnknownKey(val deviceId: DeviceId) : SyncoError("stored key for $deviceId does not match") {
        override val closeReason = CloseReason.UNKNOWN_KEY
    }

    class DidMismatch(val claimed: String) : SyncoError("static key does not hash to $claimed") {
        override val closeReason = CloseReason.DID_MISMATCH
    }

    class BadHandshake(detail: String) : SyncoError(detail) {
        override val closeReason = CloseReason.BAD_HANDSHAKE
    }

    class BadAuth(detail: String) : SyncoError(detail) {
        override val closeReason = CloseReason.BAD_AUTH
    }

    class FrameTooLarge(val length: Long) : SyncoError("frame length $length exceeds the maximum") {
        override val closeReason = CloseReason.FRAME_TOO_LARGE
    }

    class Replay(val counter: ULong) : SyncoError("record $counter failed authentication") {
        override val closeReason = CloseReason.REPLAY
    }

    class CounterExhausted : SyncoError("record counter exhausted") {
        override val closeReason = CloseReason.REPLAY
    }

    class Timeout(detail: String) : SyncoError(detail) {
        override val closeReason = CloseReason.TIMEOUT
    }

    class DuplicateSession(val deviceId: DeviceId) : SyncoError("session to $deviceId already live") {
        override val closeReason = CloseReason.DUPLICATE_SESSION
    }

    class Unpaired(val deviceId: DeviceId) : SyncoError("$deviceId is not paired") {
        override val closeReason = CloseReason.UNPAIRED
    }

    class Malformed(detail: String) : SyncoError(detail) {
        override val closeReason: CloseReason? = null
    }
}
