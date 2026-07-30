package app.synco.storage

import app.synco.protocol.message.CapsFlags
import app.synco.protocol.message.ClipRep

enum class ClipCategory {
    TEXT,
    IMAGE,
    FILE,
    ;

    companion object {
        fun of(rep: ClipRep): ClipCategory = when (rep) {
            is ClipRep.Text, is ClipRep.Html, is ClipRep.Rtf, is ClipRep.Url -> TEXT
            is ClipRep.Image -> IMAGE
            is ClipRep.File -> FILE
        }
    }
}

fun CapsFlags.allows(category: ClipCategory): Boolean = when (category) {
    ClipCategory.TEXT -> text
    ClipCategory.IMAGE -> image
    ClipCategory.FILE -> file
}
