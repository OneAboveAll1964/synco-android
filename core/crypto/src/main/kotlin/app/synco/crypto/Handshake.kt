package app.synco.crypto

import app.synco.protocol.DeviceId
import app.synco.protocol.HandshakeConstants

class Handshake(
    private val role: HandshakeRole,
    private val identity: IdentityKeyPair,
    private val ephemeral: EphemeralKeyPair,
    private val peerStaticPublicKey: ByteArray,
    private val peerEphemeralPublicKey: ByteArray,
    private val selfDeviceId: DeviceId,
    private val peerDeviceId: DeviceId,
) {
    init {
        require(selfDeviceId != peerDeviceId) { "a device may not hand shake with itself" }
        require(role == HandshakeRole.of(selfDeviceId, peerDeviceId)) {
            "role $role contradicts the device id ordering of $selfDeviceId and $peerDeviceId"
        }
    }

    fun derive(): HandshakeResult {
        val okm = CryptoPrimitives.hkdfSha256(
            ikm = inputKeyMaterial(),
            salt = salt(),
            info = ascii(HandshakeConstants.HKDF_INFO),
            length = HandshakeConstants.SESSION_OKM_BYTES,
        )
        val initiatorToResponder = okm.copyOfRange(0, HandshakeConstants.SESSION_KEY_BYTES)
        val responderToInitiator = okm.copyOfRange(
            HandshakeConstants.SESSION_KEY_BYTES,
            HandshakeConstants.SESSION_OKM_BYTES,
        )
        val sendKey = if (role == HandshakeRole.INITIATOR) initiatorToResponder else responderToInitiator
        val receiveKey = if (role == HandshakeRole.INITIATOR) responderToInitiator else initiatorToResponder
        return HandshakeResult(
            keys = SessionKeys(sendKey, receiveKey),
            confirmation = confirmationTag(sendKey, selfDeviceId),
            expectedPeerConfirmation = confirmationTag(receiveKey, peerDeviceId),
        )
    }

    private fun inputKeyMaterial(): ByteArray {
        val ephemeralEphemeral = ephemeral.agree(peerEphemeralPublicKey)
        val staticEphemeral = when (role) {
            HandshakeRole.INITIATOR -> identity.agree(peerEphemeralPublicKey)
            HandshakeRole.RESPONDER -> ephemeral.agree(peerStaticPublicKey)
        }
        val ephemeralStatic = when (role) {
            HandshakeRole.INITIATOR -> ephemeral.agree(peerStaticPublicKey)
            HandshakeRole.RESPONDER -> identity.agree(peerEphemeralPublicKey)
        }
        return (ephemeralEphemeral + staticEphemeral + ephemeralStatic).also { ikm ->
            check(ikm.size == HandshakeConstants.IKM_BYTES) { "the key schedule needs 96 bytes of input" }
        }
    }

    private fun salt(): ByteArray = CryptoPrimitives.sha256(
        ascii(minOf(selfDeviceId, peerDeviceId).value),
        ascii(maxOf(selfDeviceId, peerDeviceId).value),
    )

    private fun confirmationTag(directionKey: ByteArray, deviceId: DeviceId): ByteArray =
        CryptoPrimitives.hmacSha256(
            directionKey,
            ascii(HandshakeConstants.CONFIRM_PREFIX) + ascii(deviceId.value),
        )

    private fun ascii(value: String): ByteArray = value.toByteArray(Charsets.US_ASCII)
}
