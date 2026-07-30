package app.synco.discovery

internal object DiscoveryTuning {
    const val RESOLVE_ATTEMPTS = 3
    const val RESOLVE_RETRY_DELAY_MILLIS = 300L
    const val RESOLVE_TIMEOUT_MILLIS = 5_000L
    const val UNREGISTER_TIMEOUT_MILLIS = 2_000L
    const val REGISTER_TIMEOUT_MILLIS = 10_000L
    const val BROWSE_RESTART_DELAY_MILLIS = 1_000L
    const val BROWSE_REFRESH_MILLIS = 30_000L
    const val PEER_SWEEP_MILLIS = 10_000L
    const val PEER_EXPIRY_MILLIS = 90_000L
}
