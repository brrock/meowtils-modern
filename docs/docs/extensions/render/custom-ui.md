# Custom extension UI

Shared rendering support lives in the main Meowtils SDK under `wtf.tatp.meowtils.extension.render`. Compile against the parent project; do not copy or shade these classes into an extension. There is no separate `port-support` extension to install.

## Draw rounded panels

Use the graphics object from `RenderGameOverlayEvent` or a Minecraft screen's `extractRenderState`. Scope the shared drawing context with `try/finally`:

```java
import wtf.tatp.meowtils.extension.render.Draw;
import wtf.tatp.meowtils.extension.render.Shapes;

@EventTarget
public void onHud(RenderGameOverlayEvent event) {
    if (mc.level == null) return;
    Draw.begin(event.getGraphics());
    try {
        Shapes.round(10, 10, 140, 28, 4, 0xCC23252B);
        Shapes.gradient(10, 40, 140, 4, 2,
                0xFF61C2A2, 0xFF41826C, 0xFF61C2A2, 0xFF41826C);
        HudFont.draw(event.getGraphics(), "Extension panel", 18, 18, 0.8f, -1);
    } finally {
        Draw.end();
    }
}
```

Coordinates are GUI pixels. `Shapes.gradient` takes top-left, top-right, bottom-left, and bottom-right ARGB colors. `Shapes.outline` adds a rounded outline. Graphics poses and scissors are copied into the queued render state, so balancing pose pushes/pops and scissors remains the caller's responsibility.

## Textures and original fonts

Load extension resources using `ExtensionResources.open(MyExtension.class, "/path/in/archive.png")`, or the module's `openResource(path)`. The extension's classloader resolves its own resources.

`extension.render.DynamicTexture` uploads a `BufferedImage` into Minecraft's texture manager. `identifier(handle)` exposes the native `Identifier`; the compatibility handle returned by `func_110552_b()` is **not** an OpenGL texture name. Extension-owned uploads are released when that extension's classloader is unloaded. Do not manually call `closeAll()` from an extension: it is client-wide cleanup.

The [Tenacity GUI example](../examples.md) retains its original AWT glyph baking, advances, bundled TTFs, icons, and UI components. Its renderer submits through this SDK rather than calling OpenGL.

## Blur

```java
Blur.region(event.getGraphics(), 10, 10, 140, 28, 4);
```

Call before drawing the panel. Regions share one native blur pass, and the SDK restores the scene outside their rounded bounds. `Blur.background(graphics)` requests a full background blur. A full-screen request takes precedence when a screen and notification both request blur in the same frame. The native game's blur strength setting applies.

Minecraft calls a screen's `extractBackground` separately; do not call it again from `extractRenderState`.

## Porting old render helpers

`GL11`, `GlStateManager`, `OpenGlHelper`, `ScaledResolution`, `Keyboard`, `Mouse`, `GuiScreen`, `ChatAllowedCharacters`, and `MathHelper` in this package are a narrow source-port bridge used by the examples. They are not LWJGL 2 or legacy Minecraft classes. Geometry goes into `GuiGraphicsExtractor` using Minecraft's rendering pipelines, including the Metal backend. Blend/depth compatibility calls use the GUI pipeline's fixed state.

For new code, prefer native 26.2 graphics and input APIs, `Shapes`, `GuiPainter`, and `HudFont`. `Keyboard.modern(legacyCode)` and `Keyboard.legacy(glfwCode)` translate the original widgets' key codes; persist modern GLFW binds through the SDK's values.

For projected world overlays, use `WorldOverlay.project(...)` during HUD extraction and render [item icons](item-icons.md) through the supplied graphics object. See BedPlates for a complete example.
