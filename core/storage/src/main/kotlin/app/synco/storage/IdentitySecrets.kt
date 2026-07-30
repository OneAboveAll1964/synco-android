package app.synco.storage

internal interface IdentitySecrets {

    fun read(): String?

    fun write(value: String)
}
