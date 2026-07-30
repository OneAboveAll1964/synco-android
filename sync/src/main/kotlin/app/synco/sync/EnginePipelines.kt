package app.synco.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class EnginePipelines(
    private val clipboard: ClipboardPipeline,
    private val policies: PolicyPipeline,
    private val network: NetworkPipeline,
) {
    fun launch(scope: CoroutineScope, registry: PeerSessionRegistry) {
        scope.launch { clipboard.run(registry) }
        scope.launch { policies.run(registry) }
        scope.launch { network.run(registry) }
    }
}
