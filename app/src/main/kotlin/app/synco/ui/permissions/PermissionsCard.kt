package app.synco.ui.permissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.synco.R
import app.synco.sync.SyncProblem
import app.synco.ui.identity.IdentityResetButton

@Composable
fun PermissionsCard(
    missing: List<PermissionRequirement>,
    problem: SyncProblem?,
    onRequest: (PermissionRequirement) -> Unit,
    onResetIdentity: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.permissions_card_title),
                style = MaterialTheme.typography.titleMedium,
            )
            if (problem != null) {
                Text(
                    text = stringResource(explanationOf(problem)),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (problem == SyncProblem.IDENTITY_UNREADABLE) {
                IdentityResetButton(onConfirm = onResetIdentity)
            }
            if (problem != null && missing.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.onErrorContainer)
            }
            if (missing.isNotEmpty()) {
                PermissionsFlow(missing = missing, onRequest = onRequest)
            }
        }
    }
}
