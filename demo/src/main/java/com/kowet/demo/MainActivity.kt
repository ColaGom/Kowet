package com.kowet.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.colagom.kowet.WebSocketFactory
import com.colagom.kowet.stomp.Stomp
import com.colagom.kowet.stomp.StompEvent
import com.colagom.kowet.stomp.StompFrame
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class MainActivity : AppCompatActivity() {
    private val debugAdapter = DebugAdapter()

    private val session by lazy {
        val ws = WebSocketFactory.create("ws://10.0.2.2:8081/connect/websocket")
        Stomp.over(
            ws,
            scope = lifecycleScope
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btn_connect).setOnClickListener {
            session.events.onEach {
                debugAdapter.add(it)
            }.launchIn(lifecycleScope)
        }

        findViewById<View>(R.id.btn_subscribe).setOnClickListener {
            session.subscribe(
                "/topic/share/1",
                "1"
            )
        }

        findViewById<RecyclerView>(R.id.rv).apply {
            adapter = debugAdapter
        }
    }

    class StompViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv by lazy {
            itemView.findViewById<TextView>(R.id.tv)
        }

        fun bind(item: StompEvent) {
            tv.text = when (item) {
                is StompFrame -> item.encode()
                else -> item.toString()
            }
        }
    }

    class DebugAdapter : RecyclerView.Adapter<StompViewHolder>() {
        private val items = mutableListOf<StompEvent>()

        fun add(item: StompEvent) {
            items.add(item)
            notifyItemInserted(items.lastIndex)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = StompViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.row_stomp, parent, false)
        )

        override fun onBindViewHolder(holder: StompViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size
    }
}