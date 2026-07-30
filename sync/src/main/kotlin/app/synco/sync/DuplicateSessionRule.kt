package app.synco.sync

import app.synco.crypto.HandshakeRole

internal object DuplicateSessionRule {

    fun matchesRole(origin: SessionOrigin, role: HandshakeRole): Boolean = origin.dialed == role.dials

    fun accepts(existing: SessionOrigin?, incoming: SessionOrigin, role: HandshakeRole): Boolean {
        if (existing == null) return true
        return matchesRole(incoming, role) && !matchesRole(existing, role)
    }
}
