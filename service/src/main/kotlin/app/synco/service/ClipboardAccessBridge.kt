package app.synco.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import app.synco.clipboard.ClipboardCaptureStatus
import kotlinx.coroutines.flow.StateFlow

class ClipboardAccessBridge(private val context: Context) {

    val status: StateFlow<ClipboardCaptureStatus>
        get() = context.requireSyncoGraph().clipboard.status

    val isServiceEnabled: Boolean get() = AccessibilityAvailability.isEnabled(context)

    companion object {
        private const val FRAGMENT_ARG_KEY = ":settings:fragment_args_key"
        private const val SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args"

        fun settingsIntent(context: Context): Intent {
            val component = AccessibilityAvailability.component(context).flattenToString()
            val arguments = Bundle().apply { putString(FRAGMENT_ARG_KEY, component) }
            return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .putExtra(FRAGMENT_ARG_KEY, component)
                .putExtra(SHOW_FRAGMENT_ARGUMENTS, arguments)
        }
    }
}
