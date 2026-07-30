package app.synco.sync

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class OutboundAborts {

    private val aborted: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    fun record(transferId: UUID) {
        aborted.add(transferId)
    }

    fun isAborted(transferId: UUID): Boolean = aborted.contains(transferId)
}
