package app.synco.storage

import app.synco.protocol.DeviceId
import app.synco.protocol.Platform
import app.synco.protocol.encoding.Base64Codec

internal object TrustedPeerFixtures {

    val MAC = DeviceId("kbhhxmb5effwrego")
    val PHONE = DeviceId("spmf5yhlkmism7vs")
    val PHANTOM = DeviceId("wdcl4zutub2cls67")

    fun peer(
        deviceId: DeviceId,
        displayName: String = "Shko s MacBook Pro",
        rejected: Boolean = false,
    ): TrustedPeer = TrustedPeer(
        deviceId = deviceId,
        staticPublicKey = Base64Codec.encode(ByteArray(32) { it.toByte() }),
        displayName = displayName,
        platform = Platform.MACOS,
        firstPairedAtMillis = 1_753_887_112_000L,
        rejected = rejected,
    )
}
