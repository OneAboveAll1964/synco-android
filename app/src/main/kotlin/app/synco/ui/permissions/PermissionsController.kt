package app.synco.ui.permissions

import androidx.compose.runtime.Stable

@Stable
class PermissionsController(
    val missing: List<PermissionRequirement>,
    private val onRequest: (PermissionRequirement) -> Unit,
) {
    val hasBlockers: Boolean get() = missing.isNotEmpty()

    fun request(requirement: PermissionRequirement) {
        onRequest(requirement)
    }
}
