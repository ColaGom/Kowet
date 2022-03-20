package com.kowet.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.colagom.kowet.WebSocketFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val ws = WebSocketFactory.create("ws://10.0.2.2:8081/connect/websocket")
        ws.open().onEach {
            println(it.toString())
        }.launchIn(lifecycleScope)
    }
}