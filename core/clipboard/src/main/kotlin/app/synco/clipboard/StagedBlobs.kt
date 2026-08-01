package app.synco.clipboard

fun interface StagedBlobs {
    fun discard(snapshot: ClipboardSnapshot)

    companion object {
        val NONE = StagedBlobs { }
    }
}
