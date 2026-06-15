package com.joyproxy.app.vpn

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

object VpnStatusBus {
    sealed class Event {
        data object Connecting : Event()
        data object Connected : Event()
        data object Disconnected : Event()
        data class Failed(val message: String) : Event()
    }

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 8)
    val events: SharedFlow<Event> = _events.asSharedFlow()

    fun setConnected(connected: Boolean) {
        _connected.value = connected
    }

    fun emit(event: Event) {
        _events.tryEmit(event)
        when (event) {
            is Event.Connected -> _connected.value = true
            is Event.Disconnected, is Event.Failed -> _connected.value = false
            is Event.Connecting -> Unit
        }
    }
}
