package com.colagom.kowet.stomp

import com.colagom.kowet.WebSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

sealed interface StompEvent {
    data class Message(val text: String) : StompEvent
}

interface StompSession {
    val events: Flow<StompEvent>
    suspend fun send(
        destination: String,
        message: String
    )
}

class StompSessionImpl(
    private val webSocket: WebSocket,
    private val config: StompConfig,
    scope: CoroutineScope
) : StompSession, CoroutineScope by scope {
    private val frameFactory = FrameFactory(config)
    private val _event = MutableSharedFlow<StompEvent>()
    override val events: Flow<StompEvent> = _event.asSharedFlow()

    init {
        webSocket.open().onEach {
            _event.emit(StompEvent.Message(""))
        }.launchIn(this)
    }

    override suspend fun send(destination: String, message: String) {
        val frame = frameFactory.send(destination, message)
        webSocket.open()
        webSocket.send(frame.encode())
    }
}