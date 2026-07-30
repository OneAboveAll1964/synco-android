package app.synco.storage

import android.content.Context
import app.synco.crypto.IdentityKeyPair
import app.synco.protocol.encoding.Base64Codec
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class EncryptedIdentityStore internal constructor(
    private val secrets: IdentitySecrets,
    private val dispatcher: CoroutineDispatcher,
) : IdentityStore {

    constructor(context: Context, dispatcher: CoroutineDispatcher = Dispatchers.IO) :
        this(EncryptedIdentitySecrets(context), dispatcher)

    private val guard = Mutex()

    @Volatile
    private var cached: IdentityKeyPair? = null

    override suspend fun identity(): IdentityKeyPair = cached ?: guard.withLock {
        cached ?: withContext(dispatcher) { restore() ?: generateAndPersist() }.also { cached = it }
    }

    override suspend fun replaceWithFreshIdentity(): IdentityKeyPair = guard.withLock {
        withContext(dispatcher) { generateAndPersist() }.also { cached = it }
    }

    private fun restore(): IdentityKeyPair? {
        val stored = readStored() ?: return null
        val scalar = Base64Codec.decodeOrNull(stored) ?: throw IdentityUnavailable.KeyUnreadable(null)
        return runCatching { IdentityKeyPair.fromPrivateKey(scalar) }
            .getOrElse { throw IdentityUnavailable.KeyUnreadable(it) }
    }

    private fun readStored(): String? = runCatching { secrets.read() }
        .getOrElse { throw IdentityUnavailable.StoreUnavailable(it) }

    private fun generateAndPersist(): IdentityKeyPair {
        val generated = IdentityKeyPair.generate()
        val encoded = Base64Codec.encode(generated.exportPrivateKeyForStorage())
        runCatching { secrets.write(encoded) }
            .getOrElse { throw IdentityUnavailable.NotPersisted(it) }
        if (readStored() != encoded) throw IdentityUnavailable.NotPersisted(null)
        return generated
    }
}
