package app.synco.transport

interface PairingApproval {
    suspend fun approve(peer: PeerDescriptor): Boolean
}
