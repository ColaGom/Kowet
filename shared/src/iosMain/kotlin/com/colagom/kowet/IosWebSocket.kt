package com.colagom.kowet

import kotlinx.coroutines.flow.Flow


actual class WebSocketImpl : WebSocket {
    override fun open(): Flow<WebSocket.Event> {
        TODO("Not yet implemented")
    }

    override fun send(message: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}

actual object WebSocketFactory : WebSocket.Factory {
    override fun create(): WebSocket = TODO()
}