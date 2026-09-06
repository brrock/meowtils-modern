# Render item icons

Use `GuiGraphicsExtractor.item(...)` in `RenderGameOverlayEvent`. Minecraft renders the current item model and lighting.

```java
@EventTarget
public void onHud(RenderGameOverlayEvent event) {
    if (mc.level == null || mc.gui.screen() != null) return;
    var graphics = event.getGraphics();
    graphics.pose().pushMatrix();
    try {
        graphics.pose().translate(10, 10);
        graphics.pose().scale(0.8f, 0.8f);
        graphics.item(new ItemStack(Items.DIAMOND_SWORD), 0, 0);
    } finally {
        graphics.pose().popMatrix();
    }
}
```

The item occupies 16 × 16 GUI pixels before scaling. See [BedPlates](../examples.md) for projected, distance-scaled item plates. The old no-graphics `Render.renderItemIcon(...)` API is not used on 26.2.
