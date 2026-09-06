# RenderGameOverlayEvent

Fires each HUD frame. Extends `HudRenderEvent`.

**Cancellable:** no

```java
@EventTarget
public void onHud(RenderGameOverlayEvent event) {
    if (mc.player == null || mc.level == null) return;
    if (mc.gui.screen() != null) return;
    Meowtils.drawString(event.getGraphics(), "Example HUD text", 10, 10, 1.0f, 0xFFFFFFFF);
}
```

`getGraphics()` is `GuiGraphicsExtractor`. `getDelta()` is the frame `DeltaTracker`.
