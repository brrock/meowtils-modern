# RenderStringEvent

Mutable text hook dispatched by native font preparation for both plain strings and `FormattedCharSequence`, including outlined text. Plain-string width measurement also dispatches it. This works independently of AccountHider.

**Cancellable:** no — replace text with `setString(...)`.

```java
@EventTarget
public void onRenderString(RenderStringEvent event) {
    if (mc.player == null || event.getString() == null) return;
    String name = mc.player.getGameProfile().name();
    String text = event.getString();
    if (text.contains(name)) event.setString(text.replace(name, "Meow"));
}
```

Styled sequences are represented with legacy section formatting codes for the event, then rebuilt if changed. Unchanged sequences keep their original styles and identity. The bridge also preserves RGB colors using `§x§R§R§G§G§B§B` encoding. Replacement text can include `§a`, `§c`, etc.

This is a rendering hook, not a chat-received event. It may run repeatedly for measurement, shadows and rendering; avoid side effects, networking and repeated appends. Do not call font measurement or text drawing from the handler, which would recurse. Custom extension fonts and already prepared/cached text do not necessarily pass through this hook.

[StatsFaker](../examples.md) uses session checks and sidebar detection to limit its changes.
