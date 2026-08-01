package app.synco.shizuku

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShizukuInstallTest {

    private val nothing = ShizukuInstall(hasPackage = { false }, hasProvider = { false })

    @Test
    fun `a live binder alone proves shizuku is installed`() {
        assertTrue(nothing.isInstalled(binderAlive = true))
    }

    @Test
    fun `nothing installed and no binder reads as not installed`() {
        assertFalse(nothing.isInstalled(binderAlive = false))
    }

    @Test
    fun `the manager package counts, not only the privileged one`() {
        val install = ShizukuInstall(
            hasPackage = { it == "moe.shizuku.manager" },
            hasProvider = { false },
        )

        assertTrue(install.isInstalled(binderAlive = false))
    }

    @Test
    fun `a resolvable provider counts even when the package stays hidden`() {
        val install = ShizukuInstall(
            hasPackage = { false },
            hasProvider = { it == "moe.shizuku.privileged.api.shizuku" },
        )

        assertTrue(install.isInstalled(binderAlive = false))
    }

    @Test
    fun `a package manager that throws never reports a false negative crash`() {
        val install = ShizukuInstall(
            hasPackage = { error("dead binder") },
            hasProvider = { it == "moe.shizuku.manager.shizuku" },
        )

        assertTrue(install.isInstalled(binderAlive = false))
    }

    @Test
    fun `every probe throwing settles on not installed rather than crashing`() {
        val install = ShizukuInstall(
            hasPackage = { error("dead binder") },
            hasProvider = { error("dead binder") },
        )

        assertFalse(install.isInstalled(binderAlive = false))
    }
}
