# ClientTickEvent

Fires each client tick (usually 20 times per second).

**Cancellable:** no

## Phase

- **PRE** — before Minecraft ticks
- **POST** — after Minecraft ticks

Use **POST** unless you need to run first.

```java
@EventTarget
public void onClientTick(ClientTickEvent event) {
    if (mc.player == null || mc.level == null) return;
    if (event.getPhase() != ClientTickEvent.Phase.POST) return;
}
```

`getClient()` is the `Minecraft` instance. The module field `mc` is the same object.
