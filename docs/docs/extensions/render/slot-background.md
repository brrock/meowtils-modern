# **Render Slot Background**

This lets you render a colored background for items in inventory slots.

!!! Info

    This does not apply to hotbar slots or when you pick up the item.

---

## Interface

``` java
import wtf.tatp.meowtils.manager.slots.SlotProvider;
```

### Overrides

``` java
shouldRender(Slot slot)
```

Boolean, return true if you want to render background for this slot.

``` java
getColor(Slot slot)
```

RGB int, return what color you want it to render as. Supports alpha.

## Registering

You will need to register your implementation at init.

!!! Example

    ``` java
    SlotManager.register(new ExampleSlot());
    ```

## Example

``` java
/*
Renders dark red color for all slots that have a sword.
*/
public class ExampleSlot implements SlotProvider {

    @Override
    public boolean shouldRender(Slot slot) {
        return slot.getHasStack() && slot.getStack().getItem() instanceof ItemSword;
    }

    @Override
    public int getColor(Slot slot) {
        return ColorUtil.getRGBFromFormatting(EnumChatFormatting.DARK_RED);
    }
}
```