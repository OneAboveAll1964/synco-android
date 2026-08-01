package app.synco.transport

import app.synco.crypto.EphemeralKeyPair
import app.synco.crypto.Handshake
import app.synco.crypto.HandshakeRole
import app.synco.crypto.PeerIdentity
import app.synco.protocol.HandshakeConstants
import app.synco.protocol.ProtocolConstants
import app.synco.protocol.SyncoError
import app.synco.protocol.encoding.Base64Codec
import app.synco.protocol.message.Auth
import app.synco.protocol.message.Hello
import app.synco.protocol.message.PairRequest

internal class SessionHandshake(
    private val local: LocalDevice,
    private val frames: FramedConnection,
    private val trustedPeers: TrustedPeers,
) {
    private val ephemeral = EphemeralKeyPair.generate()

    suspend fun exchangeHello(): Hello {
        frames.write(
            Hello(
                deviceId = local.deviceId,
                displayName = local.displayName,
                platform = local.platform,
                ephemeralPublicKey = Base64Codec.encode(ephemeral.publicKey),
            ),
        )
        val message = ControlFrames.read(frames)
        val peerHello = message as? Hello ?: throw ControlFrames.unexpected("hello", message)
        if (peerHello.version != ProtocolConstants.VERSION) {
            throw SyncoError.VersionMismatch(peerHello.version)
        }
        if (peerHello.deviceId == local.deviceId) {
            throw SyncoError.SelfConnection(peerHello.deviceId)
        }
        return peerHello
    }

    suspend fun trustedKeyFor(peerHello: Hello): ByteArray? {
        val stored = trustedPeers.staticPublicKey(peerHello.deviceId) ?: return null
        val usable = stored.size == HandshakeConstants.X25519_KEY_BYTES &&
            PeerIdentity.matches(stored, peerHello.deviceId)
        if (!usable) throw SyncoError.UnknownKey(peerHello.deviceId)
        return stored
    }

    suspend fun authenticate(peerHello: Hello, peerStaticKey: ByteArray): HandshakeOutcome {
        val role = HandshakeRole.of(local.deviceId, peerHello.deviceId)
        val derived = Handshake(
            role = role,
            identity = local.identity,
            ephemeral = ephemeral,
            peerStaticPublicKey = peerStaticKey,
            peerEphemeralPublicKey = peerEphemeralKeyOf(peerHello),
            selfDeviceId = local.deviceId,
            peerDeviceId = peerHello.deviceId,
        ).derive()
        frames.write(Auth(Base64Codec.encode(derived.confirmationTag)))
        val auth = when (val step = awaitAuth()) {
            is AuthStep.PeerWantsPairing -> return HandshakeOutcome.Unpaired(step.request)
            is AuthStep.Confirmed -> step.auth
        }
        derived.requirePeerTag(confirmationTagOf(auth))
        return HandshakeOutcome.Established(
            EstablishedSession(
                peer = PeerDescriptor(
                    deviceId = peerHello.deviceId,
                    displayName = peerHello.displayName,
                    platform = peerHello.platform,
                    staticPublicKey = peerStaticKey,
                ),
                role = role,
                ciphers = derived.ciphers(),
            ),
        )
    }

    private suspend fun awaitAuth(): AuthStep =
        when (val message = ControlFrames.read(frames)) {
            is Auth -> AuthStep.Confirmed(message)
            is PairRequest -> AuthStep.PeerWantsPairing(message)
            else -> throw ControlFrames.unexpected("auth", message)
        }

    private sealed interface AuthStep {
        data class Confirmed(val auth: Auth) : AuthStep

        data class PeerWantsPairing(val request: PairRequest) : AuthStep
    }

    private fun peerEphemeralKeyOf(peerHello: Hello): ByteArray =
        decodeFixed(peerHello.ephemeralPublicKey, HandshakeConstants.X25519_KEY_BYTES)
            ?: throw SyncoError.BadHandshake(
                "the peer ephemeral key is not ${HandshakeConstants.X25519_KEY_BYTES} base64 encoded bytes",
            )

    private fun confirmationTagOf(auth: Auth): ByteArray =
        decodeFixed(auth.tag, HandshakeConstants.CONFIRM_TAG_BYTES)
            ?: throw SyncoError.BadAuth(
                "the peer confirmation tag is not ${HandshakeConstants.CONFIRM_TAG_BYTES} base64 encoded bytes",
            )

    private fun decodeFixed(encoded: String, size: Int): ByteArray? =
        Base64Codec.decodeOrNull(encoded)?.takeIf { it.size == size }
}
