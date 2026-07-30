package app.synco.sync

import app.synco.transport.LocalDevice
import app.synco.transport.PairingApproval
import app.synco.transport.SyncoClient
import app.synco.transport.SyncoSocketFactory
import app.synco.transport.TrustedPeers

class SessionEndpointFactory(
    private val sockets: SyncoSocketFactory,
    private val client: SyncoClient,
    private val trustedPeers: TrustedPeers,
    private val approval: PairingApproval,
) {
    fun create(local: LocalDevice): SessionEndpoint = TransportSessionEndpoint(
        sockets = sockets,
        sessions = PeerSessionFactory(local, client, trustedPeers, approval),
    )
}
