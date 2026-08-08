package app.synco.storage

import app.synco.crypto.PeerIdentity
import app.synco.protocol.DeviceId
import app.synco.protocol.Fingerprint
import app.synco.protocol.HandshakeConstants
import app.synco.protocol.Platform
import app.synco.protocol.encoding.Base64Codec
import app.synco.protocol.message.PairRequest
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrustedPeer(
    @SerialName("did") val deviceId: DeviceId,
    @SerialName("sPub") val staticPublicKey: String,
    @SerialName("dn") val displayName: String,
    @SerialName("pl") val platform: Platform,
    @SerialName("pairedAt") val firstPairedAtMillis: Long,
    @SerialName("rejected") val rejected: Boolean = false,
    @SerialName("mHost") val manualHosts: String? = null,
    @SerialName("mPort") val manualPort: Int? = null,
) {
    val isTrusted: Boolean get() = !rejected

    val firstPairedAt: Instant get() = Instant.ofEpochMilli(firstPairedAtMillis)

    val staticPublicKeyBytes: ByteArray?
        get() = Base64Codec.decodeOrNull(staticPublicKey)
            ?.takeIf { it.size == HandshakeConstants.X25519_KEY_BYTES }

    val fingerprint: Fingerprint? get() = staticPublicKeyBytes?.let(PeerIdentity::fingerprintOf)

    val manualHostList: List<String>
        get() = manualHosts?.split(',')?.map(String::trim)?.filter(String::isNotEmpty).orEmpty()

    val keyMatchesDeviceId: Boolean
        get() = staticPublicKeyBytes?.let { PeerIdentity.matches(it, deviceId) } == true

    companion object {
        fun accepted(request: PairRequest, pairedAtMillis: Long): TrustedPeer = TrustedPeer(
            deviceId = request.deviceId,
            staticPublicKey = request.staticPublicKey,
            displayName = request.displayName,
            platform = request.platform,
            firstPairedAtMillis = pairedAtMillis,
        )

        fun rejected(request: PairRequest, pairedAtMillis: Long): TrustedPeer =
            accepted(request, pairedAtMillis).copy(rejected = true)
    }
}
