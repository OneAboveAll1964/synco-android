package app.synco.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CaptureSwitch {

    private val current = MutableStateFlow(false)

    val isOn: StateFlow<Boolean> = current.asStateFlow()

    fun set(on: Boolean) {
        current.value = on
    }

    fun turnOn() = set(true)

    fun turnOff() = set(false)
}
