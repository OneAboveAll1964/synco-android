package app.synco.ui.home

import androidx.annotation.StringRes
import app.synco.R
import app.synco.protocol.message.CapsFlags
import app.synco.storage.ClipCategory

internal object CategoryLabels {

    val order: List<ClipCategory> = listOf(ClipCategory.TEXT, ClipCategory.IMAGE, ClipCategory.FILE)

    @StringRes
    fun labelOf(category: ClipCategory): Int = when (category) {
        ClipCategory.TEXT -> R.string.category_text
        ClipCategory.IMAGE -> R.string.category_image
        ClipCategory.FILE -> R.string.category_file
    }

    fun enabledIn(flags: CapsFlags, category: ClipCategory): Boolean = when (category) {
        ClipCategory.TEXT -> flags.text
        ClipCategory.IMAGE -> flags.image
        ClipCategory.FILE -> flags.file
    }
}
