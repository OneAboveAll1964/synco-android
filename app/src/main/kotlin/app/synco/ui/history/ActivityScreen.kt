package app.synco.ui.history

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.synco.R
import app.synco.storage.ClipHistoryEntry
import app.synco.sync.SyncEvent

@Composable
fun ActivityScreen(
    history: List<SyncEvent>,
    clips: List<ClipHistoryEntry>,
    onClipTap: (ClipHistoryEntry) -> Unit,
    nowMillis: Long,
    modifier: Modifier = Modifier,
) {
    if (history.isEmpty() && clips.isEmpty()) {
        ActivityEmpty(modifier = modifier.fillMaxSize())
        return
    }
    val context = LocalContext.current
    val days = remember(history) { history.groupBy { DayText.key(it.atMillis) } }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp),
    ) {
        if (clips.isNotEmpty()) {
            item(key = "clips-header") {
                DayHeader(label = context.getString(R.string.clips_title))
            }
            itemsIndexed(items = clips, key = { index, clip -> "clip-$index-${clip.atMillis}" }) { index, clip ->
                ClipRow(entry = clip, onTap = onClipTap)
                if (index < clips.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(start = 66.dp))
                }
            }
        }
        days.forEach { (day, events) ->
            item(key = "day-$day") {
                DayHeader(label = DayText.of(context, day, nowMillis))
            }
            itemsIndexed(items = events, key = { index, event -> "$day-$index-${event.kind}" }) { index, event ->
                ActivityRow(event = event)
                if (index < events.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(start = 70.dp))
                }
            }
        }
    }
}
