package app.synco.sync

internal class RecordingEvents : SyncEventSink {

    val events = mutableListOf<SyncEvent>()

    val kinds: List<SyncEvent.Kind> get() = events.map { it.kind }

    val last: SyncEvent? get() = events.lastOrNull()

    override fun record(event: SyncEvent) {
        events += event
    }
}
