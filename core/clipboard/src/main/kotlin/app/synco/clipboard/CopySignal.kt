package app.synco.clipboard

class CopySignal(
    val kind: CopySignalKind,
    val timestampMillis: Long,
    val packageName: String? = null,
    val className: String? = null,
    val text: String? = null,
    val contentDescription: String? = null,
)
