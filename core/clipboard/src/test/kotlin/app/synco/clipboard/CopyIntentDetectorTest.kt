package app.synco.clipboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CopyIntentDetectorTest {

    private fun detector(attempts: Int = 2) =
        CopyIntentDetector(
            ownPackageName = OWN,
            excludedPackages = { setOf(IME, SYSTEM_UI) },
            keyboardPackages = { setOf(IME) },
            attemptsPerGesture = { attempts },
        )

    private fun click(atMillis: Long, packageName: String = OTHER) =
        CopySignal(CopySignalKind.CLICK, atMillis, packageName)

    private fun window(atMillis: Long, packageName: String = OTHER) =
        CopySignal(CopySignalKind.WINDOW_STATE_CHANGED, atMillis, packageName)

    private fun toast(atMillis: Long, packageName: String = SYSTEM_UI) =
        CopySignal(CopySignalKind.COPY_TOAST, atMillis, packageName)

    @Test
    fun aWindowChangeSoonAfterAGestureIsACandidate() {
        val detector = detector()
        detector.observe(click(atMillis = 100))
        assertTrue(detector.observe(window(atMillis = 300)))
    }

    @Test
    fun aCopiedToastAfterASelectionGestureTriggersTheRead() {
        val detector = detector()
        detector.observe(click(atMillis = 100))

        assertTrue(detector.observe(toast(atMillis = 700)))
    }

    @Test
    fun theToastStillTriggersAfterWindowNoiseDrainedTheAttempts() {
        val detector = detector()
        detector.observe(click(atMillis = 100))
        assertTrue(detector.observe(window(atMillis = 600)))
        assertTrue(detector.observe(window(atMillis = 1_200)))
        assertFalse(detector.observe(window(atMillis = 1_900)))

        assertTrue(detector.observe(toast(atMillis = 2_400)))
    }

    @Test
    fun theToastIsNotBlockedByTheSpacingRule() {
        val detector = detector()
        detector.observe(click(atMillis = 100))
        assertTrue(detector.observe(window(atMillis = 600)))

        assertTrue(detector.observe(toast(atMillis = 700)))
    }

    @Test
    fun aToastLongAfterTheGestureIsIgnored() {
        val detector = detector()
        detector.observe(click(atMillis = 100))

        assertFalse(detector.observe(toast(atMillis = 30_000)))
    }

    @Test
    fun aStrayToastWithNoGestureNeverTriggers() {
        assertFalse(detector().observe(toast(atMillis = 700)))
    }

    @Test
    fun aKeyboardToastStillTriggers_theKeyboardCopiesTooOnSamsung() {
        val detector = detector()
        detector.observe(click(atMillis = 100))

        assertTrue(detector.observe(toast(atMillis = 700, packageName = IME)))
    }

    @Test
    fun ourOwnToastNeverTriggers() {
        val detector = detector()
        detector.observe(click(atMillis = 100))

        assertFalse(detector.observe(toast(atMillis = 700, packageName = OWN)))
    }

    @Test
    fun aWindowChangeWithoutAGestureIsIgnored() {
        assertFalse(detector().observe(window(atMillis = 300)))
    }

    @Test
    fun aSecondWindowChangeStillTriesWhenNothingWasCaptured() {
        val detector = detector()
        detector.observe(click(atMillis = 100))
        assertTrue(detector.observe(window(atMillis = 300)))
        assertTrue(detector.observe(window(atMillis = 1_500)))
    }

    @Test
    fun aSuccessfulCaptureDisarmsTheGesture() {
        val detector = detector()
        detector.observe(click(atMillis = 100))
        assertTrue(detector.observe(window(atMillis = 300)))
        detector.captured()
        assertFalse(detector.observe(window(atMillis = 1_500)))
    }

    @Test
    fun aGestureIsBoundedToTwoAttempts() {
        val detector = detector()
        detector.observe(click(atMillis = 100))
        assertTrue(detector.observe(window(atMillis = 300)))
        assertTrue(detector.observe(window(atMillis = 1_500)))
        assertFalse(detector.observe(window(atMillis = 2_500)))
    }

    @Test
    fun rapidWindowChangesAreSpacedOut() {
        val detector = detector()
        detector.observe(click(atMillis = 100))
        assertTrue(detector.observe(window(atMillis = 300)))
        assertFalse(detector.observe(window(atMillis = 350)))
    }

    @Test
    fun aWindowChangeLongAfterAGestureIsIgnored() {
        val detector = detector()
        detector.observe(click(atMillis = 100))
        assertFalse(detector.observe(window(atMillis = 30_000)))
    }

    @Test
    fun aKeyboardTapNeverArmsAGestureOfItsOwn() {
        val detector = detector()
        detector.observe(click(atMillis = 100, packageName = IME))
        assertFalse(detector.observe(window(atMillis = 300)))
    }

    @Test
    fun theSelectionToolbarClosingIsWhatCatchesACopy() {
        val detector = detector()
        detector.observe(click(atMillis = 100))

        assertTrue(detector.observe(window(atMillis = 300, packageName = SYSTEM_UI)))
    }

    @Test
    fun aKeyboardWindowNeverRetries_soTypingDoesNotFlashTheOverlay() {
        val detector = detector()
        detector.observe(click(atMillis = 100))

        assertFalse(detector.observe(window(atMillis = 300, packageName = IME)))
    }

    @Test
    fun ourOwnOverlayNeverTriggers() {
        val detector = detector()
        detector.observe(click(atMillis = 100))
        assertFalse(detector.observe(window(atMillis = 300, packageName = OWN)))
    }

    private companion object {
        const val OWN = "com.shkomaghdid.synco.android"
        const val OTHER = "com.sec.android.gallery3d"
        const val IME = "com.samsung.android.honeyboard"
        const val SYSTEM_UI = "com.android.systemui"
    }
}
