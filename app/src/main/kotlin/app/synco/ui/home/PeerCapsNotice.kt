package app.synco.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.synco.R
import app.synco.storage.ClipCategory

@Composable
fun PeerCapsNotice(peer: PeerRow, modifier: Modifier = Modifier) {
    val refused = refusedCategories(peer)
    if (refused.isEmpty()) return
    val names = refused.map { stringResource(CategoryLabels.labelOf(it)) }.joinToString(", ")
    Text(
        text = stringResource(R.string.peer_caps_notice, peer.displayName, names),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

private fun refusedCategories(peer: PeerRow): List<ClipCategory> {
    val accepts = peer.peerAccepts ?: return emptyList()
    if (!peer.direction.sends) return emptyList()
    return CategoryLabels.order.filter { category ->
        CategoryLabels.enabledIn(peer.send, category) &&
            !CategoryLabels.enabledIn(accepts, category)
    }
}
