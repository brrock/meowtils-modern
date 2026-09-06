# RenderStringEvent

Mutable string hook. Posted only where the port still fires it (tab / some name replacements). Do not assume every vanilla string goes through this event.

**Cancellable:** no

```java
@EventTarget
public void onRenderString(RenderStringEvent event) {
    if (mc.player == null || event.getString() == null) return;
    String name = mc.player.getGameProfile().name();
    String text = event.getString();
    if (text.contains(name)) event.setString(text.replace(name, "Meow"));
}
```
