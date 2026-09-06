# **SlotClickEvent**

---

This fires each time you click a slot in an inventory.

**Cancellable =** `true`

## Parameter

`SlotClickEvent`

## Properties

### Type

#### Click type

_This is the type of action when clicking a slot._

- **CLICK_NORMAL**

- **CLICK_SHIFT**

- **CLICK_HOTBAR**

- **CLICK_MIDDLE**

- **CLICK_DROP**

- **CLICK_DRAG**

- **CLICK_DOUBLE**

#### Button type

_This is the mouse button type when clicking a slot._

- **BUTTON_LEFT**

- **BUTTON_RIGHT**

- **BUTTON_MIDDLE**

## Methods

### Gui container

``` java
GuiContainer getGuiContainer()
```

Returns current `GuiContainer`.

### Slot

``` java
Slot getSlot()
```

Returns current `Slot`.

### Slot id

``` java
int getSlotId()
```

Returns current slot id.

### Click type

#### Get

``` java
int getClickType()
```

Returns click type.

!!! Tip

    It is recommended that you use the click type enums shown above for clarity.

#### Set

``` java
void setClickType(int clickType)
```

Sets the current click type.

!!! Warning

    When changing click type you should make sure the button type is also set in a way that would align with how a vanilla client behaves.

!!! Tip

    It is recommended that you use the click type enums shown above for clarity.

### Clicked button

#### Get

``` java
int getClickedButton()
```

Returns clicked button.

!!! Tip

    It is recommended that you use the button type enums shown above for clarity.

#### Set

``` java
void setClickedButton(int clickedButton)
```

Sets the current clicked button.

!!! Warning

    When changing button type you should make sure the click type is also set in a way that would align with how a vanilla client behaves.

!!! Tip

    It is recommended that you use the button type enums shown above for clarity.

### Replace click

#### Get

``` java
boolean getReplaceClick()
```

Returns true if click is set to be replaced.

#### Set

``` java
 setReplaceClick()
```

Replaces all clicks with middle clicks.

!!! Tip

    This prevents the clientside pick-up animation for items and allows you to click items faster since you no longer hold items, makes more of a difference when your latency is higher.

## Example

``` java
/*
This replaces all clicks in a chest with a left + shift click, making it so
you always directly shift-click items into your inventory.
*/
@EventTarget
public void onSlotClick(SlotClickEvent event) {
    if (event.getGuiContainer() instanceof GuiChest) { // Limited to chests only
        event.setClickedButton(SlotClickEvent.BUTTON_LEFT); // Left click
        event.setClickType(SlotClickEvent.CLICK_SHIFT); // Shift click
    }
}
```