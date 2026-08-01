package app.synco.protocol.message

object MessageType {
    const val DISCRIMINATOR = "t"

    const val HELLO = "hello"
    const val AUTH = "auth"
    const val PAIR_REQUEST = "pairRequest"
    const val PAIR_RESPONSE = "pairResponse"
    const val CAPS = "caps"
    const val POLICY = "policy"
    const val PING = "ping"
    const val PONG = "pong"
    const val CLIP = "clip"
    const val TRANSFER_START = "transferStart"
    const val TRANSFER_END = "transferEnd"
    const val TRANSFER_ABORT = "transferAbort"
    const val TRANSFER_PROGRESS = "transferProgress"
    const val ACK = "ack"
    const val BYE = "bye"

    val KNOWN: Set<String> = setOf(
        HELLO,
        AUTH,
        PAIR_REQUEST,
        PAIR_RESPONSE,
        CAPS,
        POLICY,
        PING,
        PONG,
        CLIP,
        TRANSFER_START,
        TRANSFER_END,
        TRANSFER_ABORT,
        TRANSFER_PROGRESS,
        ACK,
        BYE,
    )
}
