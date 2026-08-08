package app.synco.transport

import app.synco.crypto.PeerIdentity
import app.synco.protocol.DeviceId
import app.synco.protocol.HandshakeConstants
import app.synco.protocol.ProtocolConstants
import app.synco.protocol.SyncoError
import app.synco.protocol.encoding.Base64Codec
import app.synco.protocol.message.ControlMessage
import app.synco.protocol.message.Auth
import app.synco.protocol.message.Hello
import app.synco.protocol.message.PairRequest
import app.synco.protocol.message.PairResponse
import kotlinx.coroutines.withTimeoutOrNull

private const val MAX_SKIPPED_FRAMES = 8

internal class PairingExchange(
    private val local: LocalDevice,
    private val frames: FramedConnection,
    private val approval: PairingApproval,
) {
    suspend fun run(peerHello: Hello, pendingRequest: PairRequest? = null): PairingResult {
        frames.write(localPairRequest(PairTokens.tokenFor(peerHello.deviceId)))
        val request = pendingRequest ?: awaitPairRequest()
        val peer = descriptorOf(request, peerHello.deviceId)
        val approved = approval.approve(peer)
        frames.write(
            PairResponse(
                accepted = approved,
                deviceId = local.deviceId,
                staticPublicKey = Base64Codec.encode(local.staticPublicKey),
            ),
        )
        if (!approved) return PairingResult.DeclinedLocally(peer.deviceId)
        val response = awaitPairResponse(peer.deviceId)
        return if (response.accepted) {
            PairingResult.Approved(peer)
        } else {
            PairingResult.DeclinedByPeer(peer.deviceId)
        }
    }

    private fun localPairRequest(token: String?): PairRequest = PairRequest(
        deviceId = local.deviceId,
        displayName = local.displayName,
        platform = local.platform,
        staticPublicKey = Base64Codec.encode(local.staticPublicKey),
        fingerprint = local.fingerprint,
        token = token,
    )

    private suspend fun awaitPairRequest(): PairRequest {
        repeat(MAX_SKIPPED_FRAMES) {
            when (val message = awaitMessage()) {
                is PairRequest -> return message
                is Auth, is Hello -> Unit
                else -> throw ControlFrames.unexpected("pairRequest", message)
            }
        }
        throw SyncoError.Timeout("the peer never sent a pairRequest")
    }

    private suspend fun awaitPairResponse(expected: DeviceId): PairResponse {
        repeat(MAX_SKIPPED_FRAMES) {
            when (val message = awaitMessage()) {
                is PairResponse -> {
                    if (message.deviceId != expected) {
                        throw SyncoError.DidMismatch(message.deviceId.value)
                    }
                    return message
                }

                is Auth, is Hello, is PairRequest -> Unit
                else -> throw ControlFrames.unexpected("pairResponse", message)
            }
        }
        throw SyncoError.Timeout("the peer never sent a pairResponse")
    }

    private suspend fun awaitMessage(): ControlMessage =
        withTimeoutOrNull(ProtocolConstants.PAIR_TIMEOUT_MILLIS) { ControlFrames.read(frames) }
            ?: throw SyncoError.Timeout(
                "the pairing exchange stalled for ${ProtocolConstants.PAIR_TIMEOUT_MILLIS}ms",
            )

    private fun descriptorOf(request: PairRequest, expected: DeviceId): PeerDescriptor {
        if (request.deviceId != expected) throw SyncoError.DidMismatch(request.deviceId.value)
        val staticKey = Base64Codec.decodeOrNull(request.staticPublicKey)
            ?.takeIf { it.size == HandshakeConstants.X25519_KEY_BYTES }
            ?: throw SyncoError.DidMismatch(request.deviceId.value)
        if (!PeerIdentity.matches(staticKey, request.deviceId)) {
            throw SyncoError.DidMismatch(request.deviceId.value)
        }
        if (PeerIdentity.fingerprintOf(staticKey) != request.fingerprint) {
            throw SyncoError.DidMismatch(request.deviceId.value)
        }
        return PeerDescriptor(
            deviceId = request.deviceId,
            displayName = request.displayName,
            platform = request.platform,
            staticPublicKey = staticKey,
        )
    }
}
