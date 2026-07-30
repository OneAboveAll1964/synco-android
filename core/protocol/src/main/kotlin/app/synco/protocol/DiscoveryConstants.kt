package app.synco.protocol

object DiscoveryConstants {
    const val SERVICE_TYPE = "_synco._tcp"
    const val DOMAIN = "local."

    const val TXT_KEY_VERSION = "v"
    const val TXT_KEY_DEVICE_ID = "did"
    const val TXT_KEY_DISPLAY_NAME = "dn"
    const val TXT_KEY_PLATFORM = "pl"
    const val TXT_KEY_FINGERPRINT = "fp"

    const val DISPLAY_NAME_MAX_BYTES = 63

    const val RECONNECT_BASE_DELAY_MILLIS = 1_000L
    const val RECONNECT_FACTOR = 2L
    const val RECONNECT_CAP_MILLIS = 30_000L
}
