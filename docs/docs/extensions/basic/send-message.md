# Sending Messages

## Client (only you see it)

```java
Meowtils.addMessage("hello");        // prefixed
Meowtils.addCleanMessage("hello"); // no prefix
Meowtils.addChat(component);         // raw Component
Meowtils.debugMessage("hello");      // GUI debug mode
```

## Server

```java
Meowtils.sendMessage("hello");       // chat, or a /command as a server packet
Meowtils.sendCleanMessage("/who");   // same as sendMessage
```

Commands starting with `/` go out as `ServerboundChatCommandPacket` so Fabric client commands do not re-enter (this is how `/wdr` from clickable chat works).

## Log

```java
Meowtils.info("fetched stats");
Meowtils.warn("retry");
Meowtils.error("failed");
Meowtils.fatal("gave up");
```
