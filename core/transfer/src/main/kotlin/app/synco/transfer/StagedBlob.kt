package app.synco.transfer

import java.io.File

class StagedBlob(
    val file: File,
    val sha256: String,
    val size: Long,
)
