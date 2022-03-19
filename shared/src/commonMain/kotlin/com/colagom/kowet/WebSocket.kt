package com.colagom.kowet

import kotlinx.coroutines.flow.Flow


interface WebSocket {

    fun open(): Flow<Event>

    fun send(message: String): Boolean

    fun close() : Boolean

    sealed interface Event {
        object OnOpen : Event
        data class OnMessage(
            val message: String
        ) : Event

        data class OnFailure(
            val e: Throwable
        ) : Event

        data class OnClosing(
            val reason: ShutdownReason
        ) : Event

        data class OnClosed(
            val reason: ShutdownReason
        ) : Event
    }

    interface Factory {
        fun create(url: String): WebSocket
    }
}

expect class WebSocketImpl : WebSocket
expect object WebSocketFactory : WebSocket.Factory