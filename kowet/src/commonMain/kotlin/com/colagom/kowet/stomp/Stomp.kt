package com.colagom.kowet.stomp

import com.colagom.kowet.WebSocket
import kotlinx.coroutines.CoroutineScope

object Stomp {
    fun over(
        webSocket: WebSocket,
        config: StompConfig = StompConfig { },
        scope: CoroutineScope
    ): StompSession = StompSessionImpl(webSocket, config, scope)
}