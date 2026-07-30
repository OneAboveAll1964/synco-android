package app.synco.storage

sealed class IdentityUnavailable(message: String, cause: Throwable?) : Exception(message, cause) {

    class StoreUnavailable(cause: Throwable?) : IdentityUnavailable(
        "the secure identity store could not be opened or read",
        cause,
    )

    class KeyUnreadable(cause: Throwable?) : IdentityUnavailable(
        "a device identity is stored but cannot be decoded",
        cause,
    )

    class NotPersisted(cause: Throwable?) : IdentityUnavailable(
        "a new device identity could not be durably stored",
        cause,
    )
}
