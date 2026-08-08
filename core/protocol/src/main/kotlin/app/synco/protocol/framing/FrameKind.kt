package app.synco.protocol.framing

enum class FrameKind(val code: Byte) {
    CONTROL(0x01),
    BLOB(0x02),
    MEDIA(0x03),
    ;

    companion object {
        fun fromCode(code: Byte): FrameKind? = entries.firstOrNull { it.code == code }
    }
}
