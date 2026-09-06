# HUD Text

Draw in `RenderGameOverlayEvent` / `HudRenderEvent` and pass the graphics object. The no-graphics `Meowtils.drawString(text, x, y, scale, color)` overload does nothing on 26.2.

```java
@EventTarget
public void onHud(RenderGameOverlayEvent event) {
    if (mc.level == null || mc.gui.screen() != null) return;
    Meowtils.drawString(event.getGraphics(), "Meow!", 10, 10, 1.0f, 0xFFFFFFFF);
    // or
    HudFont.draw(event.getGraphics(), "Meow!", 10, 10, 1.0f, 0xFFFFFFFF);
}
```

Line spacing:

```java
int y = 10;
for (String line : lines) {
    Meowtils.drawString(event.getGraphics(), line, 10, y, 1.0f, 0xFFFFFFFF);
    y += Meowtils.offsetString(1.0f);
}
```

Font follows Settings → Smooth font. The Click GUI always uses the TTF.
