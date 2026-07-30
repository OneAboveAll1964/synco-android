package app.synco.logging

object Failures {

    fun describe(cause: Throwable): String {
        val name = cause::class.qualifiedName ?: cause::class.simpleName ?: UNKNOWN
        val detail = cause.message?.takeIf { it.isNotBlank() }
        return if (detail == null) name else "$name: $detail"
    }

    private const val UNKNOWN = "UnknownThrowable"
}
