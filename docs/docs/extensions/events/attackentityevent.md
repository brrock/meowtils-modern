# **AttackEntityEvent**

---

This fires each time you attempt to attack any entity.

**Cancellable =** `false`

## Parameter

`AttackEntityEvent`

## Methods

### Player

``` java
EntityPlayer getPlayer()
```

Returns `EntityPlayer`.

### Target

``` java
Entity getTarget()
```

Returns the target `Entity`.

## Example

``` java
@EventTarget
public void onAttackEntity(AttackEntityEvent event) {
    if (event.getTarget() == null) return;
    Meowtils.addMessage("Attacked: " + event.getTarget());
}
```