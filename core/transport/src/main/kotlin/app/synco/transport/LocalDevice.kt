package app.synco.transport

import app.synco.crypto.IdentityKeyPair
import app.synco.protocol.DeviceId
import app.synco.protocol.Fingerprint
import app.synco.protocol.Platform

class LocalDevice(
    val identity: IdentityKeyPair,
    val displayName: String,
    val platform: Platform = Platform.ANDROID,
) {
    val deviceId: DeviceId get() = identity.deviceId

    val fingerprint: Fingerprint get() = identity.fingerprint

    val staticPublicKey: ByteArray get() = identity.publicKey

    override fun toString(): String = "LocalDevice($deviceId, $displayName, ${platform.wireValue})"
}
