package app.synco.protocol.message

import app.synco.protocol.DeviceId
import app.synco.protocol.Fingerprint
import app.synco.protocol.Platform

object MessageFixtures {
    const val DEVICE_ID = "abcdefghij234567"
    const val PEER_DEVICE_ID = "zyxwvutsrq765432"
    const val PUBLIC_KEY = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8="
    const val TRANSFER_ID = "3f2a1b0c-4d5e-6f70-8192-a3b4c5d6e7f8"
    const val CLIP_HASH = "cf0057cec3abe111b602c29926e4422b021a35a62bfce928c73f0092b61a72e3"

    val HELLO = Hello(
        deviceId = DeviceId(DEVICE_ID),
        displayName = "Pixel",
        platform = Platform.ANDROID,
        ephemeralPublicKey = PUBLIC_KEY,
    )

    val CLIP = Clip(
        id = "0d3e7c1a-1111-2222-3333-444455556666",
        timestampMillis = 1_730_300_000_000L,
        origin = DeviceId(DEVICE_ID),
        hash = CLIP_HASH,
        reps = listOf(
            ClipRep.Html("<b>hi</b>"),
            ClipRep.Text("hi"),
            ClipRep.Rtf("e1xydGYxfQ=="),
            ClipRep.Url("https://example.com", title = "Example"),
            ClipRep.Image("image/png", "shot.png", 91_234L, "aabbcc", TRANSFER_ID),
            ClipRep.File("text/plain", "notes.txt", 12L, "0f1e2d", TRANSFER_ID, rel = "docs/notes.txt"),
        ),
    )

    val ALL: List<ControlMessage> = listOf(
        HELLO,
        Auth(tag = PUBLIC_KEY),
        PairRequest(
            deviceId = DeviceId(DEVICE_ID),
            displayName = "Pixel",
            platform = Platform.ANDROID,
            staticPublicKey = PUBLIC_KEY,
            fingerprint = Fingerprint("A1B2-C3D4-E5F6-0718"),
        ),
        PairResponse(accepted = true, deviceId = DeviceId(PEER_DEVICE_ID), staticPublicKey = PUBLIC_KEY),
        PairResponse(accepted = false, deviceId = DeviceId(PEER_DEVICE_ID), staticPublicKey = PUBLIC_KEY),
        Caps(accepts = CapsFlags.ALL_ENABLED, sends = CapsFlags(text = true, image = true, file = false)),
        Caps(accepts = CapsFlags.ALL_DISABLED, sends = CapsFlags.ALL_DISABLED, maxBlobBytes = 1L),
        Caps(accepts = CapsFlags.ALL_ENABLED, sends = CapsFlags.ALL_ENABLED, adbShizuku = true),
        Caps(
            accepts = CapsFlags.ALL_ENABLED,
            sends = CapsFlags.ALL_ENABLED,
            maxBlobBytes = Long.MAX_VALUE,
        ),
        PolicySync(
            revision = 1_785_450_000_000L,
            send = CapsFlags.ALL_ENABLED,
            receive = CapsFlags(text = true, image = false, file = true),
        ),
        PolicySync(
            revision = 1L,
            send = CapsFlags.ALL_DISABLED,
            receive = CapsFlags.ALL_DISABLED,
            paused = true,
            maxBlobBytes = Long.MAX_VALUE,
        ),
        ShizukuStartRequest,
        ShizukuStartResult(started = true),
        ShizukuStartResult(started = false, reason = "adbMissing"),
        Ping(12),
        Pong(12),
        CLIP,
        TransferStart(TRANSFER_ID, CLIP.id, "shot.png", "image/png", 91_234L, "aabbcc"),
        TransferEnd(TRANSFER_ID, ok = true),
        TransferProgressReport(TRANSFER_ID, 8_388_608L),
        TransferAbort.of(TRANSFER_ID, AckReason.TOO_LARGE),
        TransferAbort(TRANSFER_ID),
        Ack.applied(CLIP.id),
        Ack.rejected(CLIP.id, AckReason.TYPE_DISABLED),
        Bye.of(CloseReason.SHUTDOWN),
        Bye(),
    )
}
