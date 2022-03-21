//
//  ContentView.swift
//  iosDemo
//
//  Created by Geunho on 2022/03/21.
//

import SwiftUI
import StompClientLib
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
        }
    }
    
//    var socketClient = StompClientLib()
//    let delegate = StompDelegate.init()
    
//    private let url = URL(string: "ws://localhost:8081/connect/websocket")!
    
    
//    func connect() {
//        socketClient.openSocketWithURLRequest(
//            request: NSURLRequest(url: url),
//            delegate: self.delegate
//        )
//    }
//
//    func subscribe() {
//        socketClient.subscribeWithHeader(destination: "/topic/share/1", withHeader: ["id" : "1"])
//    }
    
    //
        @State var wrapper : SocketWrapper? = nil
    
        func connect() {
            let ws = WebSocketFactory_.shared.create(url: "ws://localhost:8081/connect/websocket")
            self.wrapper = SocketHelperKt.stomp(webSocket: ws)
            self.wrapper?.watch { event in
                print(event)
            }
        }
    
        func subscribe() {
            print("subscribe")
            self.wrapper?.subscribe(destination: "/topic/share/1", id: "1")
        }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}

class StompDelegate : StompClientLibDelegate {
    func stompClient(client: StompClientLib!, didReceiveMessageWithJSONBody jsonBody: AnyObject?, akaStringBody stringBody: String?, withHeader header:[String:String]?, withDestination destination: String) {
        print("Destination : \(destination)")
        print("JSON Body : \(String(describing: jsonBody))")
        print("String Body : \(stringBody ?? "nil")")
    }
    
    func stompClientDidDisconnect(client: StompClientLib!) {
        print("stompClientDidDisconnect")
        
    }
    func stompClientDidConnect(client: StompClientLib!) {
        print("stompClientDidConnect")
        client.subscribe(destination: "/topic/share/1")
    }
    func serverDidSendReceipt(client: StompClientLib!, withReceiptId receiptId: String) {
        print("Receipt : \(receiptId)")
    }
    func serverDidSendError(client: StompClientLib!, withErrorMessage description: String, detailedErrorMessage message: String?) {
        print("Error Send : \(String(describing: message))")
    }
    func serverDidSendPing() {
        print("Server ping")
    }
}
