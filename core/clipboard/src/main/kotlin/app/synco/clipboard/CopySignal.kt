package app.synco.clipboard

class CopySignal(
    val kind: CopySignalKind,
    val timestampMillis: Long,
    val packageName: String? = null,
)
