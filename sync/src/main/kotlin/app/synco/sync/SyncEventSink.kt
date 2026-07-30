package app.synco.sync

interface SyncEventSink {
    fun record(event: SyncEvent)
}
