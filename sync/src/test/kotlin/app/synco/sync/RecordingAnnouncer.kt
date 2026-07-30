package app.synco.sync

internal class RecordingAnnouncer : ReceivedFileAnnouncer {

    val arrivals = mutableListOf<ReceivedFileArrival>()

    override fun announce(arrival: ReceivedFileArrival) {
        arrivals += arrival
    }
}
