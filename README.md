## Hinote
### UI
- Drawing space
- Tools selection
- Fonts selection
- Predefined Colors selection (2 types one for Light mode and Dark mode)
- Vertical column for chatting with users in the room
- Room participants viewer
- Finally necessary logic to display all of these features
### Networking
- Users can create rooms where other users can join
- Project will use the server-client architecture
- Server will synchronize updates of every users in a room
- Server will update every room concurently that has been created (multithreading)
- Uses Websockets rather than simple HTTP polling
- How it works:

Client: Establish connection
Server: (keeps connection open)
Server: → Send update immediately when available
Client: → Send updates as they happen

- Custom message protocol (JSON) for sending and recieving updates from Server
- Example:
```JSON
// Client-side outgoing message format
{
  "type": "operation",
  "id": "unique-op-id",       // Client-generated unique ID
  "operation": {
    "type": "draw-line",
    "startX": 100, "startY": 100,
    "endX": 200, "endY": 200,
    "color": "#FF0000"
  },
  "timestamp": 1634567890000  // Client local timestamp
}
```
### Future improvements
- Markdown support
```
hinote/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── hinote/
│   │   │           ├── HinoteApplication.java
│   │   │           ├── client/
│   │   │           │   ├── HinoteClient.java
│   │   │           │   ├── ui/
│   │   │           │   │   ├── MainController.java
│   │   │           │   │   ├── components/
│   │   │           │   │   │   ├── ChatPanel.java
│   │   │           │   │   │   ├── DrawingCanvas.java
│   │   │           │   │   │   ├── ToolsPanel.java
│   │   │           │   │   │   ├── ParticipantsPanel.java
│   │   │           │   │   │   └── ColorPalette.java
│   │   │           │   │   └── dialogs/
│   │   │           │   │       ├── JoinRoomDialog.java
│   │   │           │   │       └── CreateRoomDialog.java
│   │   │           │   ├── network/
│   │   │           │   │   ├── WebSocketClient.java
│   │   │           │   │   ├── MessageHandler.java
│   │   │           │   │   └── ConnectionManager.java
│   │   │           │   └── models/
│   │   │           │       ├── Room.java
│   │   │           │       ├── User.java
│   │   │           │       ├── ChatMessage.java
│   │   │           │       ├── DrawingOperation.java
│   │   │           │       └── TextOperation.java
│   │   │           ├── server/
│   │   │           │   ├── HinoteServer.java
│   │   │           │   ├── handlers/
│   │   │           │   │   ├── WebSocketHandler.java
│   │   │           │   │   ├── RoomHandler.java
│   │   │           │   │   └── MessageRouter.java
│   │   │           │   ├── models/
│   │   │           │   │   ├── ServerRoom.java
│   │   │           │   │   ├── ConnectedUser.java
│   │   │           │   │   └── OperationHistory.java
│   │   │           │   └── services/
│   │   │           │       ├── RoomService.java
│   │   │           │       ├── UserService.java
│   │   │           │       └── SynchronizationService.java
│   │   │           ├── shared/
│   │   │           │   ├── protocol/
│   │   │           │   │   ├── Message.java
│   │   │           │   │   ├── MessageType.java
│   │   │           │   │   ├── ChatMessageProtocol.java
│   │   │           │   │   ├── DrawingOperationProtocol.java
│   │   │           │   │   ├── TextOperationProtocol.java
│   │   │           │   │   └── RoomProtocol.java
│   │   │           │   └── utils/
│   │   │           │       ├── JsonUtil.java
│   │   │           │       └── IdGenerator.java
│   │   │           └── config/
│   │   │               ├── ClientConfig.java
│   │   │               └── ServerConfig.java
│   │   └── resources/
│   │       ├── fxml/
│   │       │   ├── main.fxml
│   │       │   ├── chat-panel.fxml
│   │       │   ├── tools-panel.fxml
│   │       │   └── participants-panel.fxml
│   │       ├── css/
│   │       │   ├── light-theme.css
│   │       │   └── dark-theme.css
│   │       └── icons/
│   │           ├── brush.png
│   │           ├── pen.png
│   │           └── eraser.png
│   └── test/
│       └── java/
│           └── com/
│               └── hinote/
│                   ├── client/
│                   │   └── network/
│                   │       └── WebSocketClientTest.java
│                   ├── server/
│                   │   └── handlers/
│                   │       └── WebSocketHandlerTest.java
│                   └── shared/
│                       └── protocol/
│                           └── MessageTest.java

```
```
// Text operation example
{
  "type": "TEXT_OPERATION",
  "id": "unique-op-id",
  "operation": {
    "operationType": "CREATE_TEXT",
    "textId": "text-123",
    "x": 100,
    "y": 200,
    "content": "Hello World!",
    "fontSize": 16,
    "fontFamily": "Arial",
    "fontWeight": "bold",
    "color": "#FF0000",
    "rotation": 0
  },
  "timestamp": 1634567890000
}
```

