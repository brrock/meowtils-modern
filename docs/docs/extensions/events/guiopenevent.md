# **GuiOpenEvent**

---

This fires each time the player opens a gui.

**Cancellable =** `false`

## Parameter

`GuiOpenEvent`

## Methods

### Gui

``` java
GuiScreen getGui()
```

Returns current `GuiScreen`.

## Example

``` java
@EventTarget
public void onGuiOpen(GuiOpenEvent event) {
    if (event.getGui() instanceof GuiChest) {
        Meowtils.addMessage("You opened a chest!");
    }
}
```