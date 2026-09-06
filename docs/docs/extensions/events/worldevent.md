# **WorldEvent**

---

This fires each time a world loads or unloads for you.

**Cancellable =** `false`

## Parameter

`WorldEvent`

## Properties

### Type

- **LOAD**

- **UNLOAD**

## Methods

### Type

``` java
Type getType()
```

Returns the type shown above.

### World

``` java
WorldClient getWorld()
```

Returns `WorldClient`.

## Example

``` java
@EventTarget
public void onWorldLoad(WorldEvent event) {
    if (event.getType() != WorldEvent.Type.LOAD) return;
    Meowtils.addMessage("World loaded!");
}
```