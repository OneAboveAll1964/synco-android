package app.synco.transport

import app.synco.crypto.HandshakeRole
import app.synco.crypto.SessionCipherPair

internal class EstablishedSession(
    val peer: PeerDescriptor,
    val role: HandshakeRole,
    val ciphers: SessionCipherPair,
)
