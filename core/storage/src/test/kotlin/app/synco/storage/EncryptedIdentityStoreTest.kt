package app.synco.storage

import app.synco.crypto.IdentityKeyPair
import app.synco.protocol.encoding.Base64Codec
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class EncryptedIdentityStoreTest {

    @Test
    fun `an empty store generates one identity and persists it before returning`() = runTest {
        val secrets = FakeIdentitySecrets()
        val store = storeFor(secrets)

        val created = store.identity()

        assertEquals(1, secrets.writes)
        assertEquals(created.deviceId, persistedIn(secrets).deviceId)
        assertSame(created, store.identity())
        assertEquals(1, secrets.writes)
    }

    @Test
    fun `a later process restores the stored identity instead of generating another`() = runTest {
        val secrets = FakeIdentitySecrets()
        val created = storeFor(secrets).identity()

        val restored = storeFor(secrets).identity()

        assertEquals(created.deviceId, restored.deviceId)
        assertEquals(1, secrets.writes)
    }

    @Test
    fun `an undecodable stored value raises the typed failure and keeps the bytes`() = runTest {
        val secrets = FakeIdentitySecrets(NOT_BASE64)
        val store = storeFor(secrets)

        val failure = runCatching { store.identity() }.exceptionOrNull()

        assertTrue(failure is IdentityUnavailable.KeyUnreadable)
        assertEquals(NOT_BASE64, secrets.stored)
        assertEquals(0, secrets.writes)
    }

    @Test
    fun `a stored value of the wrong length raises the typed failure and keeps the bytes`() = runTest {
        val truncated = Base64Codec.encode(ByteArray(8))
        val secrets = FakeIdentitySecrets(truncated)
        val store = storeFor(secrets)

        val failure = runCatching { store.identity() }.exceptionOrNull()

        assertTrue(failure is IdentityUnavailable.KeyUnreadable)
        assertEquals(truncated, secrets.stored)
        assertEquals(0, secrets.writes)
    }

    @Test
    fun `a secure store that cannot be opened raises the typed failure without generating`() = runTest {
        val secrets = FakeIdentitySecrets(readFailure = IllegalStateException("key store invalidated"))
        val store = storeFor(secrets)

        val failure = runCatching { store.identity() }.exceptionOrNull()

        assertTrue(failure is IdentityUnavailable.StoreUnavailable)
        assertEquals(0, secrets.writes)
    }

    @Test
    fun `a rejected write raises the typed failure rather than returning an unsaved identity`() = runTest {
        val secrets = FakeIdentitySecrets(writeFailure = IllegalStateException("disk full"))
        val store = storeFor(secrets)

        val failure = runCatching { store.identity() }.exceptionOrNull()

        assertTrue(failure is IdentityUnavailable.NotPersisted)
        assertNull(secrets.stored)
    }

    @Test
    fun `a silently dropped write raises the typed failure rather than returning an unsaved identity`() = runTest {
        val secrets = FakeIdentitySecrets(acceptsWrites = false)
        val store = storeFor(secrets)

        val failure = runCatching { store.identity() }.exceptionOrNull()

        assertTrue(failure is IdentityUnavailable.NotPersisted)
        assertNull(secrets.stored)
    }

    @Test
    fun `concurrent callers share one generated identity`() = runTest {
        val secrets = FakeIdentitySecrets()
        val store = storeFor(secrets)

        val first = async { store.identity() }
        val second = async { store.identity() }

        assertSame(first.await(), second.await())
        assertEquals(1, secrets.writes)
    }

    @Test
    fun `an explicit reset is the only path that replaces an unreadable identity`() = runTest {
        val secrets = FakeIdentitySecrets(NOT_BASE64)
        val store = storeFor(secrets)
        runCatching { store.identity() }
        assertEquals(NOT_BASE64, secrets.stored)

        val fresh = store.replaceWithFreshIdentity()

        assertEquals(1, secrets.writes)
        assertEquals(fresh.deviceId, persistedIn(secrets).deviceId)
        assertSame(fresh, store.identity())
    }

    private fun TestScope.storeFor(secrets: FakeIdentitySecrets): EncryptedIdentityStore =
        EncryptedIdentityStore(secrets, StandardTestDispatcher(testScheduler))

    private fun persistedIn(secrets: FakeIdentitySecrets): IdentityKeyPair =
        IdentityKeyPair.fromPrivateKey(Base64Codec.decode(requireNotNull(secrets.stored)))

    private companion object {
        const val NOT_BASE64 = "this is not a stored key"
    }
}
