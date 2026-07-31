package app.synco.storage

enum class BlobSizeChoice(val bytes: Long) {
    TEN_MEGABYTES(10_485_760L),
    FIFTY_MEGABYTES(52_428_800L),
    HUNDRED_MEGABYTES(104_857_600L),
    TWO_FIFTY_MEGABYTES(262_144_000L),
    FIVE_HUNDRED_MEGABYTES(524_288_000L),
    ONE_GIGABYTE(1_073_741_824L),
    ;

    companion object {
        fun nearest(bytes: Long): BlobSizeChoice = entries.minBy { distance(it.bytes, bytes) }

        private fun distance(option: Long, bytes: Long): Long =
            if (option > bytes) option - bytes else bytes - option
    }
}
