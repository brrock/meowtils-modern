# **MouseInputEvent**

---

This fires each time you press a mouse button.

**Cancellable =** `true`

## Parameter

`MouseInputEvent`

## Properties

### Action

- **RIGHT_CLICK**

- **LEFT_CLICK**

- **MIDDLE_CLICK**

## Methods

``` java
Action getAction()
```

Returns current action as shown above.

## Example

``` java
@EventTarget
public void onMouseInput(MouseInputEvent event) {
    if (event.getAction() == MouseInputEvent.Action.MIDDLE_CLICK) {
        Meowtils.addMessage("Prevented you from middle clicking!");
        event.setCancelled(true);
    }
}
```