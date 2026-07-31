package app.synco.shizuku

enum class ShizukuState {
    NOT_INSTALLED,
    NOT_RUNNING,
    PERMISSION_DENIED,
    READY,
    ;

    val isUsable: Boolean get() = this == READY
}
