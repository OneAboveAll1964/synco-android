package app.synco.ui.permissions

import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.LifecycleOwner

internal fun Context.findLifecycleOwner(): LifecycleOwner? {
    var current: Context = this
    while (true) {
        if (current is LifecycleOwner) return current
        current = (current as? ContextWrapper)?.baseContext ?: return null
    }
}
