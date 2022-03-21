//
//  ContentView.swift
//  iosDemo
//
//  Created by Geunho on 2022/03/21.
//

import SwiftUI
import shared


struct ContentView: View {
    
    var body: some View {
        VStack {
            Button(action: {
                self.connect()
            }) {
                Text("stomp")
            }
            Button(action: {
                self.subscribe()
            }) {
                Text("subscribe")
            }
            Button(action: {
                self.close()
            }) {
                Text("close")
            }
        }
    }
    
    @State var wrapper : StompWrapper? = nil
    
    func connect() {
        let ws = WebSocketFactory_.shared.create(url: "ws://localhost:8081/connect/websocket")
        self.wrapper = SocketHelperKt.stomp(webSocket: ws)
        self.wrapper?.watch { event in
            print(event)
        }
    }
    
    func subscribe() {
        self.wrapper?.subscribe(destination: "/topic/share/1")
    }
    
    func close() {
        self.wrapper?.close()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
