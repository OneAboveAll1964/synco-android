package app.synco.sync

import app.synco.protocol.message.CapsFlags
import app.synco.storage.PeerDirections

enum class SyncDirection {
    BOTH,
    OUTBOUND,
    INBOUND,
    NONE,
    ;

    val sends: Boolean get() = this == BOTH || this == OUTBOUND

    val receives: Boolean get() = this == BOTH || this == INBOUND

    fun applyTo(directions: PeerDirections): PeerDirections = PeerDirections(
        send = if (sends) enabledOr(directions.send) else CapsFlags.ALL_DISABLED,
        receive = if (receives) enabledOr(directions.receive) else CapsFlags.ALL_DISABLED,
    )

    private fun enabledOr(flags: CapsFlags): CapsFlags =
        if (flags.allDisabled) CapsFlags.ALL_ENABLED else flags

    companion object {
        fun of(directions: PeerDirections): SyncDirection = when {
            !directions.send.allDisabled && !directions.receive.allDisabled -> BOTH
            !directions.send.allDisabled -> OUTBOUND
            !directions.receive.allDisabled -> INBOUND
            else -> NONE
        }
    }
}
