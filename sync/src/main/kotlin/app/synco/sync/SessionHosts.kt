package app.synco.sync

import app.synco.transport.PairingResult
import app.synco.transport.PeerDescriptor
import app.synco.transport.PeerSession

internal interface SessionHosts {

    suspend fun claim(
        peer: PeerDescriptor,
        origin: SessionOrigin,
        session: PeerSession,
    ): SessionBinding?

    suspend fun settlePairing(result: PairingResult)
}
