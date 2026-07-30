package app.synco.clipboard

import app.synco.protocol.message.ClipRep
import app.synco.transfer.OutgoingTransfer

class PreparedRep(
    val rep: ClipRep,
    val transfer: OutgoingTransfer? = null,
)
