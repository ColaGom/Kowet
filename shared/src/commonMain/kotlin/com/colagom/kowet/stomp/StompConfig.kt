package com.colagom.kowet.stomp

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

//TODO: WIP
class StompConfig(
    initializer: StompConfig.() -> Unit
) {
    init {
        this.initializer()
    }

    var url: String = ""
    val version = V_1_2
    var debug: Boolean = true
    var connectionTimeout = 5.seconds
    var reconnectDelay = 5.seconds
    var heartbeatIncoming = Duration.ZERO
    var heartbeatOutgoing = Duration.ZERO

    companion object {
        private const val V_1_2 = "1.2"
    }
}