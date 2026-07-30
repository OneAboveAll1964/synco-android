package app.synco.clipboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CopyIntentDetectorTest {

    private fun detector(ownPackage: String? = OWN_PACKAGE) =
        CopyIntentDetector(CopyLabels(listOf("copy")), ownPackage, selectionWindowMillis = 1_000L)

    private fun click(atMillis: Long, text: String? = null, description: String? = null) =
        CopySignal(CopySignalKind.CLICK, atMillis, text = text, contentDescription = description)

    private fun longClick(atMillis: Long, text: String? = null) =
        CopySignal(CopySignalKind.LONG_CLICK, atMillis, text = text)

    private fun selection(atMillis: Long) =
        CopySignal(CopySignalKind.TEXT_SELECTION_CHANGED, atMillis)

    private fun window(atMillis: Long, packageName: String?) =
        CopySignal(CopySignalKind.WINDOW_STATE_CHANGED, atMillis, packageName = packageName)

    @Test
    fun clickOnCopyLabelIsDetected() {
        assertTrue(detector().observe(click(atMillis = 10, text = "Copy")))
    }

    @Test
    fun clickMatchesLabelCaseAndPaddingInsensitively() {
        assertTrue(detector().observe(click(atMillis = 10, text = "  COPY ")))
    }

    @Test
    fun longClickWithCopyContentDescriptionIsDetected() {
        assertTrue(detector().observe(longClick(atMillis = 5, text = "Copy")))
    }

    @Test
    fun unrelatedClickWithoutSelectionIsIgnored() {
        assertFalse(detector().observe(click(atMillis = 10, text = "Share")))
    }

    @Test
    fun clickShortlyAfterSelectionIsDetected() {
        val detector = detector()
        assertFalse(detector.observe(selection(atMillis = 100)))
        assertTrue(detector.observe(click(atMillis = 400, text = "Share")))
    }

    @Test
    fun clickLongAfterSelectionIsIgnored() {
        val detector = detector()
        detector.observe(selection(atMillis = 100))
        assertFalse(detector.observe(click(atMillis = 2_000, text = "Share")))
    }

    @Test
    fun selectionIsConsumedBySingleDetection() {
        val detector = detector()
        detector.observe(selection(atMillis = 100))
        assertTrue(detector.observe(click(atMillis = 300, text = "Share")))
        assertFalse(detector.observe(click(atMillis = 500, text = "Share")))
    }

    @Test
    fun windowChangeFromAnotherAppIsDetected() {
        assertTrue(detector().observe(window(atMillis = 500, packageName = "com.sec.android.gallery3d")))
    }

    @Test
    fun windowChangeNeedsNoPrecedingSelection() {
        assertTrue(detector().observe(window(atMillis = 500, packageName = SYSTEM_UI)))
    }

    @Test
    fun windowChangeFromOurOwnOverlayIsIgnored() {
        assertFalse(detector().observe(window(atMillis = 500, packageName = OWN_PACKAGE)))
    }

    @Test
    fun windowChangeClearsAPendingSelection() {
        val detector = detector()
        detector.observe(selection(atMillis = 100))
        detector.observe(window(atMillis = 200, packageName = OWN_PACKAGE))
        assertFalse(detector.observe(click(atMillis = 300, text = "Share")))
    }

    private companion object {
        const val SYSTEM_UI = "com.android.systemui"
        const val OWN_PACKAGE = "app.synco"
    }
}
