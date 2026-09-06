# RenderWorldLastEvent

World-space submit after the level. Extends `WorldRenderEvent`.

```java
@EventTarget
public void onWorld(RenderWorldLastEvent event) {
    event.submitOutline(cameraRelativeBox, 0xFFFF0000, 1.5f, true);
}
```

Helpers: `WorldOverlay.outline`, `WorldOverlay.filledBox`, `WorldOverlay.worldLabel`, `WorldOverlay.healthBar`. Positions are camera-relative when you call `submitOutline` / `submitFilled` directly.

No Tessellator / `GL11` in extensions.
