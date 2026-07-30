package app.synco.sync

enum class PeerConnectionStatus {
    OFFLINE,
    DISCOVERED,
    WAITING,
    CONNECTING,
    RETRYING,
    PAIRING,
    CONNECTED,
    REJECTED,
    ;

    val isLive: Boolean get() = this == CONNECTED
}
