package app.synco.sync

interface ReceivedFileAnnouncer {

    fun announce(arrival: ReceivedFileArrival)
}
