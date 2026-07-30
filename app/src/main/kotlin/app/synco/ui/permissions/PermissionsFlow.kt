package app.synco.ui.permissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun PermissionsFlow(
    missing: List<PermissionRequirement>,
    onRequest: (PermissionRequirement) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        missing.forEach { requirement ->
            PermissionItem(requirement = requirement, onRequest = onRequest)
        }
    }
}

@Composable
private fun PermissionItem(
    requirement: PermissionRequirement,
    onRequest: (PermissionRequirement) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = stringResource(requirement.titleRes),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = stringResource(requirement.explanationRes),
            style = MaterialTheme.typography.bodySmall,
            color = LocalContentColor.current.copy(alpha = EXPLANATION_ALPHA),
        )
        TextButton(
            onClick = { onRequest(requirement) },
            modifier = Modifier.align(Alignment.Start),
        ) {
            Text(text = stringResource(requirement.actionRes))
        }
    }
}

private const val EXPLANATION_ALPHA = 0.85f
