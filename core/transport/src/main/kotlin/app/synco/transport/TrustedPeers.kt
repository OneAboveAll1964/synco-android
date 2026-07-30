package app.synco.transport

import app.synco.protocol.DeviceId

interface TrustedPeers {
    suspend fun staticPublicKey(deviceId: DeviceId): ByteArray?
}
