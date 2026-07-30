package app.synco.transport

import app.synco.crypto.EphemeralKeyPair
import app.synco.crypto.Handshake
import app.synco.crypto.HandshakeRole
import app.synco.crypto.IdentityKeyPair
import app.synco.crypto.SessionCipherPair
import app.synco.protocol.DeviceId
import app.synco.protocol.Platform

internal object SessionFixtures {

    fun device(displayName: String, platform: Platform = Platform.ANDROID): LocalDevice =
        LocalDevice(IdentityKeyPair.generate(), displayName, platform)

    fun trusting(vararg peers: LocalDevice): TrustedPeers =
        StoredTrustedPeers(peers.associate { it.deviceId to it.staticPublicKey })

    fun untrusting(): TrustedPeers = StoredTrustedPeers(emptyMap())

    fun approval(accepted: Boolean): PairingApproval = object : PairingApproval {
        override suspend fun approve(peer: PeerDescriptor): Boolean = accepted
    }

    fun sessionCiphers(
        first: LocalDevice,
        second: LocalDevice,
    ): Pair<SessionCipherPair, SessionCipherPair> {
        val firstEphemeral = EphemeralKeyPair.generate()
        val secondEphemeral = EphemeralKeyPair.generate()
        return derive(first, second, firstEphemeral, secondEphemeral).ciphers() to
            derive(second, first, secondEphemeral, firstEphemeral).ciphers()
    }

    private fun derive(
        self: LocalDevice,
        peer: LocalDevice,
        selfEphemeral: EphemeralKeyPair,
        peerEphemeral: EphemeralKeyPair,
    ) = Handshake(
        role = HandshakeRole.of(self.deviceId, peer.deviceId),
        identity = self.identity,
        ephemeral = selfEphemeral,
        peerStaticPublicKey = peer.staticPublicKey,
        peerEphemeralPublicKey = peerEphemeral.publicKey,
        selfDeviceId = self.deviceId,
        peerDeviceId = peer.deviceId,
    ).derive()

    private class StoredTrustedPeers(private val keys: Map<DeviceId, ByteArray>) : TrustedPeers {
        override suspend fun staticPublicKey(deviceId: DeviceId): ByteArray? = keys[deviceId]
    }
}
