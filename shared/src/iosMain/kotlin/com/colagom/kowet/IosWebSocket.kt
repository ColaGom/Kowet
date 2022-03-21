package com.colagom.kowet

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Foundation.*
import platform.darwin.NSObject

actual class WebSocketImpl(
    url: String
) : WebSocket {
    private val socketEndpoint = NSURL.URLWithString(url)!!
    private var realSocket: NSURLSessionWebSocketTask? = null

    override fun open(): Flow<WebSocket.Event> {
        return callbackFlow {
            val urlSession = NSURLSession.sessionWithConfiguration(
                configuration = NSURLSessionConfiguration.defaultSessionConfiguration(),
                delegate = sessionDelegate(),
                delegateQueue = NSOperationQueue.currentQueue()
            )
            realSocket = urlSession.webSocketTaskWithURL(socketEndpoint)
            listenMessages(
                onError = { trySend(WebSocket.Event.OnFailure(it)) },
                onMessage = { trySend(WebSocket.Event.OnMessage(it)) }
            )
            realSocket?.resume()
            awaitClose {
                this@WebSocketImpl.close()
            }
        }
    }

    private fun listenMessages(
        onError: (Throwable) -> Unit,
        onMessage: (String) -> Unit
    ) {
        realSocket?.receiveMessageWithCompletionHandler { message, nsError ->
            when {
                nsError != null -> onError(Throwable(nsError.description))
                message != null -> message.string?.let { onMessage(it) }
            }
            listenMessages(onError, onMessage)
        }
    }

    private fun ProducerScope<WebSocket.Event>.sessionDelegate() =
        object : NSObject(), NSURLSessionWebSocketDelegateProtocol {
            override fun URLSession(
                session: NSURLSession,
                webSocketTask: NSURLSessionWebSocketTask,
                didOpenWithProtocol: String?
            ) {
                trySend(WebSocket.Event.OnOpen)
            }

            override fun URLSession(
                session: NSURLSession,
                webSocketTask: NSURLSessionWebSocketTask,
                didCloseWithCode: NSURLSessionWebSocketCloseCode,
                reason: NSData?
            ) {
                trySend(
                    WebSocket.Event.OnClosed(
                        ShutdownReason(
                            didCloseWithCode.toInt(),
                            reason.toString()
                        )
                    )
                )
            }
        }


    override fun send(message: String): Boolean {
        realSocket?.sendMessage(NSURLSessionWebSocketMessage(message)) {
            if (it != null) NSLog("%@", it)
        }

        return true
    }

    override fun close(): Boolean {
        val res = realSocket != null
        realSocket?.cancelWithCloseCode(1000, null)
        realSocket = null
        return res
    }
}

actual object WebSocketFactory : WebSocket.Factory {
    override fun create(url: String): WebSocket = WebSocketImpl(url)
}