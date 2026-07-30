package app.synco.sync

enum class SessionOrigin {
    DIALED,
    ACCEPTED,
    ;

    val dialed: Boolean get() = this == DIALED
}
