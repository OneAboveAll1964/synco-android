package app.synco.sync

import app.synco.protocol.framing.MediaFrame

fun interface RemoteMediaSink {
    fun onMediaFrame(frame: MediaFrame)

    companion object {
        val NONE = RemoteMediaSink { }
    }
}
