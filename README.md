# Kowet 
[![Maven Central](https://img.shields.io/maven-central/v/io.github.colagom/kowet.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.colagom%22%20AND%20a:%22kowet%22)

KMM WebSocket support to STOMP protocol

# STOMP?
- https://stomp.github.io/stomp-specification-1.2.html

# Usage
### Android
```kotlin
val ws = WebSocketFactory.create("ws://10.0.2.2:8081/connect/websocket")
// create session
val session = Stomp.over(
    ws,
    scope = lifecycleScope
)

// subscribe topic
session.subscribe("topic", "1")

session.events.onEach {
    //TODO: handle event
}.launchIn(lifecycleScope)
```

### iOS
```swift
let ws = WebSocketFactory_.shared.create(url: "ws://localhost:8081/connect/websocket")
//create session
let wrapper = SocketHelperKt.stomp(webSocket: ws)

//subscribe topic
wrapper.subscribe(destination: "topic")

wrapper.watch { event in
    // handle event
}
```
# Release
### Groovy DSL
```groovy
implementation 'io.github.colagom:kowet:{version}'
```
### Kotlin DSL
```kotlin
implementation("io.github.colagom:kowet:{version}")
```
