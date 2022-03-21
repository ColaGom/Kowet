package com.colagom.kowet

import com.colagom.kowet.stomp.Stomp
import com.colagom.kowet.stomp.StompEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * TODO: Fix it
 * currently, implementation just supports testing.
 */

fun interface Closeable {
    fun close()
}

class StompWrapper(
    private val webSocket: WebSocket
) : Closeable {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private val session = Stomp.over(webSocket, scope = scope)

    fun watch(block: (StompEvent) -> Unit) {
        session.events.onEach {
            block(it)
        }.launchIn(scope)
    }

    fun subscribe(destination: String) {
        session.subscribe(destination, "1")
    }

    override fun close() {
        webSocket.close()
        job.cancel()
    }
}

fun stomp(webSocket: WebSocket) = StompWrapper(webSocket)

