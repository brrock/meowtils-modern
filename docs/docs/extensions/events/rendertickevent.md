# RenderTickEvent

Once per frame, before or after the render tick. Prefer `RenderGameOverlayEvent` or `RenderWorldLastEvent` for drawing.

**Cancellable:** no

```java
@EventTarget
public void onRenderTick(RenderTickEvent event) {
    if (mc.player == null || mc.level == null) return;
    if (event.getPhase() != RenderTickEvent.Phase.POST) return;
}
```
