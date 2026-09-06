# **PlayerInteractEvent**

---

This fires each time you interact with a block in the world.

**Cancellable =** `false`

## Parameter

`PlayerInteractEvent`

## Properties

### Action

- **LEFT_CLICK_BLOCK**

- **RIGHT_CLICK_BLOCK**

## Methods

### Action

``` java
Action getAction()
```

Returns current action as shown above.

### Player

``` java
EntityPlayerSP getPlayer()
```

Returns `EntityPlayerSP`.

### World

``` java
WorldClient getWorld()
```

Returns `WorldClient`.

### Block position

``` java
BlockPos getPos()
```

Returns `BlockPos`.

### Facing

``` java
EnumFacing getFacing()
```

Returns `EnumFacing`.

## Example

``` java
/*
Sends a message when you right-click a chest.
*/
@EventTarget
public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;

    final BlockPos blockPos = event.getPos();
    final TileEntity tileEntity = mc.theWorld.getTileEntity(blockPos);

    if (tileEntity instanceof TileEntityChest) {
        Meowtils.addMessage("Right clicked chest!");
    }
}
```