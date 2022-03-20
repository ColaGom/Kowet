package com.colagom.kowet

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Foundation.*
import platform.darwin.NSObject

actual class WebSocketImpl(
    private val url: String
) : WebSocket {
    private var realSocket: NSURLSessionWebSocketTask? = null

    override fun open(): Flow<WebSocket.Event> {
        return callbackFlow {
            val urlSession = NSURLSession.sessionWithConfiguration(
                configuration = NSURLSessionConfiguration.defaultSessionConfiguration(),
                delegate = object : NSObject(), NSURLSessionWebSocketDelegateProtocol {
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
                },
                delegateQueue = NSOperationQueue.currentQueue()
            )

            realSocket = urlSession.webSocketTaskWithURL(NSURL.URLWithString(url)!!)
            realSocket?.receiveMessageWithCompletionHandler { message, nsError ->
                when {
                    nsError != null -> {
                        trySend(WebSocket.Event.OnFailure(Throwable(nsError.description)))
                    }
                    message != null -> {
                        message.string?.let {
                            trySend(WebSocket.Event.OnMessage(it))
                        }
                    }
                }
            }
            realSocket?.resume()

            awaitClose {
                close()
            }
        }
    }

    override fun send(message: String): Boolean {
        realSocket?.sendMessage(NSURLSessionWebSocketMessage(message)) {
            /** TODO : Logging **/
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