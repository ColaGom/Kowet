package com.colagom.kowet

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocketListener
import okio.ByteString


actual class WebSocketImpl(
    private val url: String
) : WebSocket {
    private var realSocket: okhttp3.WebSocket? = null

    override fun open(): Flow<WebSocket.Event> {
        val request = Request.Builder().url(url).build()
        val httpClient = OkHttpClient.Builder().build()

        return callbackFlow {
            realSocket = httpClient.newWebSocket(
                request,
                socketListener()
            )

            awaitClose {
                realSocket?.cancel()
            }
        }
    }

    override fun send(message: String): Boolean = realSocket?.send(message) ?: false
    override fun close() = realSocket?.close(1000 /*indicates a normal closure*/, null) ?: false

    private fun ProducerScope<WebSocket.Event>.socketListener() = object : WebSocketListener() {
        override fun onClosing(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
            trySend(
                WebSocket.Event.OnClosing(
                    ShutdownReason(code, reason)
                )
            )
        }

        override fun onMessage(webSocket: okhttp3.WebSocket, bytes: ByteString) {
            trySend(WebSocket.Event.OnMessage(bytes.utf8()))
        }

        override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
            trySend(WebSocket.Event.OnMessage(text))
        }

        override fun onOpen(webSocket: okhttp3.WebSocket, response: Response) {
            trySend(WebSocket.Event.OnOpen)
        }

        override fun onClosed(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
            trySend(WebSocket.Event.OnClosed(ShutdownReason(code, reason)))
        }

        override fun onFailure(
            webSocket: okhttp3.WebSocket,
            t: Throwable,
            response: Response?
        ) {
            trySend(WebSocket.Event.OnFailure(t))
        }
    }
}

actual object WebSocketFactory : WebSocket.Factory {
    override fun create(url: String): WebSocket = WebSocketImpl(url)
}