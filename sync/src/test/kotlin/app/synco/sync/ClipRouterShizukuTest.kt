package app.synco.sync

import app.synco.protocol.message.ShizukuStartRequest
import app.synco.protocol.message.ShizukuStartResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipRouterShizukuTest {

    private val link = RecordingPeerLink()
    private val reports = mutableListOf<ShizukuStartReport>()

    @Test
    fun `asking for a start sends the bare request`() = runTest {
        router().requestShizukuStart()

        assertEquals(listOf(ShizukuStartRequest), link.envelopes)
    }

    @Test
    fun `a successful result reaches the sink`() = runTest {
        router().receive(ShizukuStartResult(started = true))

        assertEquals(1, reports.size)
        assertTrue(reports.single().started)
        assertNull(reports.single().reason)
    }

    @Test
    fun `a refusal carries its reason through`() = runTest {
        router().receive(ShizukuStartResult(started = false, reason = "notAllowed"))

        assertEquals("notAllowed", reports.single().reason)
    }

    @Test
    fun `nothing is reported until a result arrives`() = runTest {
        router().requestShizukuStart()

        assertTrue(reports.isEmpty())
    }

    private fun router(): ClipRouter = ClipFixtures.router(
        link = link,
        settings = FixedPolicy(),
        shizuku = { reports += it },
    )
}
