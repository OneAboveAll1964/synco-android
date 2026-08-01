package app.synco.storage

import org.junit.Assert.assertEquals
import org.junit.Test

class BlobSizeChoiceTest {

    @Test
    fun anExactValueMapsToItsChoice() {
        assertEquals(BlobSizeChoice.HUNDRED_MEGABYTES, BlobSizeChoice.nearest(104_857_600L))
    }

    @Test
    fun anUnknownValueMapsToTheClosestChoice() {
        assertEquals(BlobSizeChoice.FIFTY_MEGABYTES, BlobSizeChoice.nearest(50_000_000L))
    }

    @Test
    fun anEnormousValueMapsToNoLimit() {
        assertEquals(BlobSizeChoice.UNLIMITED, BlobSizeChoice.nearest(Long.MAX_VALUE))
    }

    @Test
    fun aValueJustOverAGigabyteStillMapsToAGigabyte() {
        assertEquals(BlobSizeChoice.ONE_GIGABYTE, BlobSizeChoice.nearest(1_100_000_000L))
    }

    @Test
    fun onlyNoLimitIsUnlimited() {
        assertEquals(
            listOf(BlobSizeChoice.UNLIMITED),
            BlobSizeChoice.entries.filter { it.isUnlimited },
        )
    }

    @Test
    fun zeroMapsToTheSmallestChoice() {
        assertEquals(BlobSizeChoice.TEN_MEGABYTES, BlobSizeChoice.nearest(0L))
    }
}
