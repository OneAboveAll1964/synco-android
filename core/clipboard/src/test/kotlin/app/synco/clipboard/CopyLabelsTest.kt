package app.synco.clipboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CopyLabelsTest {

    @Test
    fun matchesResolvedLabelIgnoringCaseAndPadding() {
        val labels = CopyLabels.withFallback("Kopieren")
        assertTrue(labels.matches("  kopieren "))
    }

    @Test
    fun matchesKnownFallbackWhenResolutionFails() {
        val labels = CopyLabels.withFallback(null)
        assertTrue(labels.matches("Copy"))
    }

    @Test
    fun matchesKnownFallbackWhenResolutionBlank() {
        val labels = CopyLabels.withFallback("   ")
        assertTrue(labels.matches("copy link"))
    }

    @Test
    fun nullCandidateNeverMatches() {
        assertFalse(CopyLabels.withFallback("copy").matches(null))
    }

    @Test
    fun unrelatedLabelDoesNotMatch() {
        assertFalse(CopyLabels.withFallback("copy").matches("paste"))
    }
}
