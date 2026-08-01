package app.synco.transport

import app.synco.protocol.message.PairRequest

internal sealed interface HandshakeOutcome {

    data class Established(val session: EstablishedSession) : HandshakeOutcome

    data class Unpaired(val pendingRequest: PairRequest) : HandshakeOutcome
}
