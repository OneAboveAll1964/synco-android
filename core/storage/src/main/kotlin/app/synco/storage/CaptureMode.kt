package app.synco.storage

enum class CaptureMode(val wireValue: String) {
    ACCESSIBILITY("accessibility"),
    SHIZUKU("shizuku"),
    ;

    companion object {
        val DEFAULT = ACCESSIBILITY

        fun parse(value: String?): CaptureMode =
            entries.firstOrNull { it.wireValue == value } ?: DEFAULT
    }
}
