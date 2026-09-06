# ChatReceivedEvent

Fires for game chat. Overlay titles (action bar) set `isOverlay()` true.

**Cancellable:** yes — cancel to hide the line from the player.

```java
@EventTarget
public void onChat(ChatReceivedEvent event) {
    if (event.isOverlay()) return;
    String text = event.getText();
    Component component = event.getMessage();
}
```

Local lines posted with `Meowtils.addMessage` are marked so some modules skip them. Prefer `event.getText()` over assuming `§` codes; ViaVersion chat still often has them, so strip with `ColorUtil.unformattedText` when matching.
