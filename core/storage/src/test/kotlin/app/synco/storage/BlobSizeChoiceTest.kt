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
    fun anEnormousValueMapsToTheLargestChoice() {
        assertEquals(BlobSizeChoice.ONE_GIGABYTE, BlobSizeChoice.nearest(Long.MAX_VALUE))
    }

    @Test
    fun zeroMapsToTheSmallestChoice() {
        assertEquals(BlobSizeChoice.TEN_MEGABYTES, BlobSizeChoice.nearest(0L))
    }
}
