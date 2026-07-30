package app.synco.sync

import app.synco.transport.LocalDevice
import app.synco.transport.PairingApproval
import app.synco.transport.PeerSession
import app.synco.transport.SyncoClient
import app.synco.transport.TransportConnection
import app.synco.transport.TrustedPeers

class PeerSessionFactory(
    private val local: LocalDevice,
    private val client: SyncoClient,
    private val trustedPeers: TrustedPeers,
    private val approval: PairingApproval,
) {
    suspend fun dial(host: String, port: Int): PeerSession =
        PeerSession(local, client.connect(host, port), trustedPeers, approval)

    fun accept(connection: TransportConnection): PeerSession =
        PeerSession(local, connection, trustedPeers, approval)
}
