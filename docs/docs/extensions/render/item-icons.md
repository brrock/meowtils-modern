# **Render Item Icon**

!!! Note

    When rendering an icon on screen you should use [RenderGameOverlayEvent](../events/rendergameoverlayevent.md).

``` java
Render.renderItemIcon(ItemStack stack, float x, float y, float scale)
```

This will render an item icon on your screen.

## Example

!!! Tip

    You should check **Minecraft.getMinecraft().currentScreen** to limit rendering your item so it only shows up when you want it to, like in the example below which will only render the item when no GUI is open.

``` java
@EventTarget
public void onRenderGameOverlay(RenderGameOverlayEvent event) {
    if (mc.theWorld == null) return;
    if (mc.currentScreen != null) return;

    final ItemStack itemStack = new ItemStack(Items.diamond_sword);

    Render.renderItemIcon(itemStack, 10, 10, 1);
}
```