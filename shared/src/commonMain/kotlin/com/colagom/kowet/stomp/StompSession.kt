package com.colagom.kowet.stomp

import com.colagom.kowet.WebSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

interface StompSession {
    val events: Flow<StompEvent>
    fun send(
        destination: String,
        message: String
    )

    fun subscribe(
        destination: String,
        id: String,
        ack: AckMode = AckMode.AUTO
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
            when (it) {
                is WebSocket.Event.OnClosed -> {
                }
                is WebSocket.Event.OnClosing -> {
                }
                is WebSocket.Event.OnFailure -> {
                }
                is WebSocket.Event.OnMessage -> _event.emit(it.asStompFrame())
                WebSocket.Event.OnOpen -> {
                    webSocket.send(frameFactory.connect().encode())
                }
            }
        }.launchIn(this)
    }

    private fun WebSocket.Event.asStompFrame(): StompEvent = when (this) {
        is WebSocket.Event.OnMessage -> StompFrame.parse(message)
        else -> StompEvent.Error(message = this.toString())
    }

    override fun send(destination: String, message: String) {
        val frame = frameFactory.send(destination, message)
        webSocket.send(frame.encode())
    }

    override fun subscribe(destination: String, id: String, ack: AckMode) {
        val frame = frameFactory.subscribe(id, destination, ack)
        webSocket.send(frame.encode())
    }
}