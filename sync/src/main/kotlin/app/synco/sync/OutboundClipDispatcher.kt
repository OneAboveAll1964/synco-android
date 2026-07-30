package app.synco.sync

import app.synco.clipboard.ClipboardSnapshot
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class OutboundClipDispatcher(private val transfers: TransferGateway) {

    suspend fun dispatch(snapshot: ClipboardSnapshot, routers: List<ClipRouter>) {
        if (routers.isNotEmpty()) {
            coroutineScope {
                routers.forEach { router -> launch { quietly { router.send(snapshot) } } }
            }
        }
        snapshot.transfers.forEach { transfers.releaseOutgoing(it.transferId) }
    }
}
