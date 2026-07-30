package app.synco.sync

import java.util.UUID

internal class InboundClipStore {

    private val held = mutableMapOf<String, InboundClipAssembly>()
    private val owners = mutableMapOf<UUID, String>()

    fun hold(assembly: InboundClipAssembly) {
        held[assembly.clip.id] = assembly
        assembly.transferIds.forEach { owners[it] = assembly.clip.id }
    }

    fun forTransfer(transferId: UUID): InboundClipAssembly? = owners[transferId]?.let { held[it] }

    fun release(assembly: InboundClipAssembly) {
        held.remove(assembly.clip.id)
        assembly.transferIds.forEach { owners.remove(it) }
    }

    fun drain(): List<InboundClipAssembly> {
        val assemblies = held.values.toList()
        held.clear()
        owners.clear()
        return assemblies
    }
}
