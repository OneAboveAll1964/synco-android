package app.synco.ui.permissions

import androidx.annotation.StringRes
import app.synco.R
import app.synco.sync.SyncProblem

@StringRes
internal fun explanationOf(problem: SyncProblem): Int = when (problem) {
    SyncProblem.IDENTITY_UNREADABLE -> R.string.problem_identity_unreadable
    SyncProblem.CLIPBOARD_UNREADABLE -> R.string.problem_clipboard_unreadable
    SyncProblem.NETWORK_UNAVAILABLE -> R.string.problem_network_unavailable
    SyncProblem.DISCOVERY_UNAVAILABLE -> R.string.problem_discovery_unavailable
    SyncProblem.LISTENER_UNAVAILABLE -> R.string.problem_listener_unavailable
}
