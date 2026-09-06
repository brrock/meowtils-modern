# Entity ESP

Draw in `RenderWorldLastEvent`. The old `Render.drawEntityBox` helper is gone. Use `WorldOverlay` on the interpolated box.

```java
@EventTarget
public void onWorld(RenderWorldLastEvent event) {
    if (mc.player == null || mc.level == null) return;
    float partial = WorldOverlay.partialTick();
    for (Player player : mc.level.players()) {
        if (player == mc.player) continue;
        AABB box = WorldOverlay.interpolated(player, partial).inflate(0.1);
        WorldOverlay.outline(event, box, 0xFFFFFFFF, 1.5f);
    }
}
```

`WorldOverlay.filledBox` / `drawBox` add a fill. Colors are packed ARGB ints (`0xAARRGGBB`).
