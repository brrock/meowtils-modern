# NotificationEvent

Posted by both `NotificationManager.show(...)` overloads before the stock overlay is created. Cancel it to supply a replacement notification UI.

**Cancellable:** yes — suppresses the new stock notification.

```java
@EventTarget
public void onNotification(NotificationEvent event) {
    pending.add(new Notice(event.getTitle(), event.getMessage(),
            event.getType(), event.getDuration()));
    event.setCancelled(true);
}
```

`getType()` returns `NotificationManager.Type` (`INFO`, `ALERT`, `WARNING`). `getDuration()` is in milliseconds. A cancelled request does not clear an older stock notification already on screen.

The event runs on the caller's thread. Queue immutable data in a thread-safe queue and render it later from `RenderGameOverlayEvent`; do not upload textures or draw inside this event. Register the handler on an enabled module, or check your module state in an extra listener.

See [Notifications+](../examples.md) for style mapping, duplicate suppression, animation and test notifications.
