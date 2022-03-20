package com.colagom.kowet.stomp

interface StompSocket {
    fun connect(): StompSession
}

internal class StompSocketImpl() : StompSocket {
    override fun connect(): StompSession {
        TODO("Not yet implemented")
    }
}