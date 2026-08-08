package app.synco.sync

interface RemoteSignals {
    fun onRemoteAccepted(width: Int, height: Int, input: Boolean)

    fun onRemoteRejected(reason: String)

    fun onRemoteStopped()

    companion object {
        val NONE = object : RemoteSignals {
            override fun onRemoteAccepted(width: Int, height: Int, input: Boolean) = Unit
            override fun onRemoteRejected(reason: String) = Unit
            override fun onRemoteStopped() = Unit
        }
    }
}
