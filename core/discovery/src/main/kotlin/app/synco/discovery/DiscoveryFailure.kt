package app.synco.discovery

class DiscoveryFailure private constructor(
    val operation: Operation,
    val errorCode: Int?,
    message: String,
) : Exception(message) {

    enum class Operation(val label: String) {
        REGISTER("registration"),
        UNREGISTER("unregistration"),
        DISCOVER("discovery"),
        RESOLVE("resolution"),
    }

    companion object {
        fun of(operation: Operation, errorCode: Int): DiscoveryFailure =
            DiscoveryFailure(operation, errorCode, "nsd ${operation.label} failed with error code $errorCode")

        fun timedOut(operation: Operation): DiscoveryFailure =
            DiscoveryFailure(operation, null, "nsd ${operation.label} did not complete in time")
    }
}
