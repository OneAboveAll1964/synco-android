package app.synco.crypto

import app.synco.protocol.DeviceId
import app.synco.protocol.Fingerprint
import app.synco.protocol.HandshakeConstants

object PeerIdentity {

    fun deviceIdOf(staticPublicKey: ByteArray): DeviceId =
        DeviceId.fromIdentityHash(identityHash(staticPublicKey))

    fun fingerprintOf(staticPublicKey: ByteArray): Fingerprint =
        Fingerprint.fromIdentityHash(identityHash(staticPublicKey))

    fun matches(staticPublicKey: ByteArray, claimed: DeviceId): Boolean =
        deviceIdOf(staticPublicKey) == claimed

    private fun identityHash(staticPublicKey: ByteArray): ByteArray {
        require(staticPublicKey.size == HandshakeConstants.X25519_KEY_BYTES) {
            "a static public key must be ${HandshakeConstants.X25519_KEY_BYTES} bytes"
        }
        return CryptoPrimitives.sha256(staticPublicKey)
    }
}
