package app.synco.service

import app.synco.shizuku.ShizukuState
import app.synco.storage.CaptureMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CaptureOwnerTest {

    @Test
    fun `accessibility stays quiet while sync is switched off`() {
        assertFalse(
            CaptureOwner.accessibilityShouldCapture(
                syncIsOn = false,
                mode = CaptureMode.ACCESSIBILITY,
                state = ShizukuState.NOT_INSTALLED,
            ),
        )
    }

    @Test
    fun `accessibility captures once sync is switched on`() {
        assertTrue(
            CaptureOwner.accessibilityShouldCapture(
                syncIsOn = true,
                mode = CaptureMode.ACCESSIBILITY,
                state = ShizukuState.NOT_INSTALLED,
            ),
        )
    }

    @Test
    fun `Shizuku takes over from accessibility when it is usable`() {
        assertFalse(
            CaptureOwner.accessibilityShouldCapture(
                syncIsOn = true,
                mode = CaptureMode.SHIZUKU,
                state = ShizukuState.READY,
            ),
        )
    }

    @Test
    fun `accessibility covers for Shizuku when Shizuku stopped`() {
        assertTrue(
            CaptureOwner.accessibilityShouldCapture(
                syncIsOn = true,
                mode = CaptureMode.SHIZUKU,
                state = ShizukuState.NOT_RUNNING,
            ),
        )
    }

    @Test
    fun `switching sync off silences Shizuku mode too`() {
        assertFalse(
            CaptureOwner.accessibilityShouldCapture(
                syncIsOn = false,
                mode = CaptureMode.SHIZUKU,
                state = ShizukuState.NOT_RUNNING,
            ),
        )
    }
}
