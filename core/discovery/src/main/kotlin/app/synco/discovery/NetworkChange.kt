package app.synco.discovery

enum class NetworkChange {
    AVAILABLE,
    RECONFIGURED,
    UNAVAILABLE,
    ;

    val carriesLocalNetwork: Boolean get() = this != UNAVAILABLE
}
