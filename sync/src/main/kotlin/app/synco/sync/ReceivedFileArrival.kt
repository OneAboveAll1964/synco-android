package app.synco.sync

class ReceivedFileArrival(
    val names: List<String>,
    val location: String?,
    val browsable: Boolean,
) {
    val count: Int get() = names.size

    val singleName: String? get() = names.singleOrNull()
}
