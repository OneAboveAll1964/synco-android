package app.synco.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val MACOS_WIRE_VALUE = "macos"
private const val ANDROID_WIRE_VALUE = "android"

@Serializable
enum class Platform(val wireValue: String) {
    @SerialName(MACOS_WIRE_VALUE)
    MACOS(MACOS_WIRE_VALUE),

    @SerialName(ANDROID_WIRE_VALUE)
    ANDROID(ANDROID_WIRE_VALUE),
    ;

    companion object {
        fun fromWire(wireValue: String): Platform? = entries.firstOrNull { it.wireValue == wireValue }
    }
}
