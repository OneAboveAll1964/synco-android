package app.synco.sync

import app.synco.protocol.message.CapsFlags
import app.synco.storage.ClipCategory
import app.synco.storage.PeerDirections

internal object PolicyEdits {

    fun withSend(directions: PeerDirections, category: ClipCategory, enabled: Boolean): PeerDirections =
        directions.copy(send = toggled(directions.send, category, enabled))

    fun withReceive(directions: PeerDirections, category: ClipCategory, enabled: Boolean): PeerDirections =
        directions.copy(receive = toggled(directions.receive, category, enabled))

    private fun toggled(flags: CapsFlags, category: ClipCategory, enabled: Boolean): CapsFlags =
        when (category) {
            ClipCategory.TEXT -> flags.copy(text = enabled)
            ClipCategory.IMAGE -> flags.copy(image = enabled)
            ClipCategory.FILE -> flags.copy(file = enabled)
        }
}
