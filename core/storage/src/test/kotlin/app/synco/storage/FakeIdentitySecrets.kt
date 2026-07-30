package app.synco.storage

internal class FakeIdentitySecrets(
    initial: String? = null,
    private val readFailure: Throwable? = null,
    private val writeFailure: Throwable? = null,
    private val acceptsWrites: Boolean = true,
) : IdentitySecrets {

    var stored: String? = initial
        private set

    var reads: Int = 0
        private set

    var writes: Int = 0
        private set

    override fun read(): String? {
        reads++
        readFailure?.let { throw it }
        return stored
    }

    override fun write(value: String) {
        writes++
        writeFailure?.let { throw it }
        if (acceptsWrites) stored = value
    }
}
