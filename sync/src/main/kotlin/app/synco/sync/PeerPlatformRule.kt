package app.synco.sync

import app.synco.protocol.Platform

internal object PeerPlatformRule {

    fun pairs(self: Platform, peer: Platform): Boolean = self != peer
}
