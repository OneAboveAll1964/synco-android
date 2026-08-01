package app.synco.service

import app.synco.shizuku.ShizukuState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect

object ShizukuStatus {

    private val current = MutableStateFlow(ShizukuState.NOT_INSTALLED)

    val state: StateFlow<ShizukuState> = current.asStateFlow()

    suspend fun publish(source: StateFlow<ShizukuState>) {
        source.collect { current.value = it }
    }
}
