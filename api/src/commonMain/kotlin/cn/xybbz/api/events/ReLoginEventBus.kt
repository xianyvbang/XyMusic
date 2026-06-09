package cn.xybbz.api.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ReLoginEventBus {

    private val _events = MutableSharedFlow<ReLoginEvent>(
        extraBufferCapacity = 1
    )

    val events = _events.asSharedFlow()

    fun notify(event: ReLoginEvent) {
        _events.tryEmit(event)
    }
}
